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
package com.github.j5ik2o.adceet.adaptor.healthchecks

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.HealthCheck.Severity

import scala.concurrent.Future
import scala.util.Try

package object core {
  type HealthCheckResult = ValidatedNel[String, Unit]

  def healthy: HealthCheckResult = ().validNel[String]

  def unhealthy(msg: String): HealthCheckResult = msg.invalidNel[Unit]

  def healthCheck(
      name: String,
      severity: Severity = Severity.Fatal
  )(c: => HealthCheckResult): HealthCheck =
    new HealthCheck(name, Future.fromTry(Try(c)), severity)

  def asyncHealthCheck(
      name: String,
      severity: Severity = Severity.Fatal
  )(c: => Future[HealthCheckResult]): HealthCheck =
    new HealthCheck(name, c, severity)
}
