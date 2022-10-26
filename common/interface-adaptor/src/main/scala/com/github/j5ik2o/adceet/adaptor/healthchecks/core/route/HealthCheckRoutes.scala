/*
 * Copyright 2022 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.j5ik2o.adceet.adaptor.healthchecks.core.route
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{ OK, ServiceUnavailable }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives
import akka.http.scaladsl.server.{ PathMatchers, Route }
import cats.data.Validated.{ Invalid, Valid }
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.{ HealthCheck, HealthCheckResult }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.JsonObject
import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

object HealthCheckRoutes extends FailFastCirceSupport {

  case class HealthCheckResultJson(name: String, severity: String, status: String, messages: List[String])

  case class ResponseJson(status: String, check_results: List[HealthCheckResultJson])

  private def status(s: Boolean): String = if (s) "healthy" else "unhealthy"

  private def statusCode(s: Boolean): StatusCode = if (s) OK else ServiceUnavailable

  private def toResultJson(check: HealthCheck, result: HealthCheckResult): HealthCheckResultJson =
    HealthCheckResultJson(
      check.name,
      check.severity.toString,
      status(result.isValid),
      result match {
        case Valid(_)        => List()
        case Invalid(errors) => errors.toList
      }
    )

  def health(
      checks: HealthCheck*
  )(implicit
      ec: ExecutionContext
  ): Route = health("health", checks.toList)

  def health(
      path: String,
      checks: List[HealthCheck]
  )(implicit
      ec: ExecutionContext
  ): Route = {
    require(checks.nonEmpty, "checks must not empty.")
    require(
      checks.map(_.name).toSet.size == checks.length,
      s"HealthCheck name should be unique (given HealthCheck names = [${checks.map(_.name).mkString(",")}])."
    )
    val rootSlashRemoved =
      if (path.startsWith("/")) path.substring(1) else path
    PathDirectives.path(PathMatchers.separateOnSlashes(rootSlashRemoved)) {
      parameter("full" ? false) { full =>
        get {
          def isHealthy(checkAndResults: List[(HealthCheck, HealthCheckResult)]) =
            checkAndResults.forall(cr => cr._2.isValid || (!cr._1.severity.isFatal))
          val checkAndResultsFuture = Future.traverse(checks) { c => c.run().map(c -> _) }
          if (full) {
            val result = checkAndResultsFuture.map { checkAndResults =>
              val healthy = isHealthy(checkAndResults)
              statusCode(healthy) -> ResponseJson(
                status(healthy),
                checkAndResults.map { case (check, result) =>
                  toResultJson(check, result)
                }
              )
            }
            onSuccess(result) { (check, results) =>
              complete(check, results)
            }
          } else {
            val result = checkAndResultsFuture.map { checkAndResults =>
              statusCode(isHealthy(checkAndResults)) -> JsonObject.empty
            }
            onSuccess(result) { (check, results) =>
              complete(check, results)
            }
          }
        }
      }
    }
  }
}
