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

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.HealthCheck.Severity
import com.github.j5ik2o.adceet.adaptor.healthchecks.core._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures

class HealthCheckRoutesTest extends AnyFunSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  describe("HealthCheck route") {
    it("should raise exception when no healthcheck is given.") {
      val exception = the[IllegalArgumentException] thrownBy HealthCheckRoutes
        .health()
      exception.getMessage shouldEqual "requirement failed: checks must not empty."
    }

    it("should raise exception when given healthchecks have same names") {
      val exception = the[IllegalArgumentException] thrownBy HealthCheckRoutes.health(
        healthCheck("test")(healthy),
        healthCheck("test")(healthy)
      )
      exception.getMessage shouldEqual "requirement failed: HealthCheck name should be unique (given HealthCheck names = [test,test])."
    }

    it("should return correct healthy response when all healthchecks are healthy.") {
      val ok1 = healthCheck("test1")(healthy)
      val ok2 = healthCheck("test2")(healthy)

      Get("/health") ~> HealthCheckRoutes.health(ok1, ok2) ~> check {
        status shouldEqual OK
        responseAs[String] shouldEqual "{}"
      }

      Get("/health?full=true") ~> HealthCheckRoutes.health(ok1, ok2) ~> check {
        status shouldEqual OK
        responseAs[String] shouldEqual
          """
            |{
            |  "status": "healthy",
            |  "check_results": [
            |    { "name": "test1", "severity": "Fatal", "status": "healthy", "messages": [] },
            |    { "name": "test2", "severity": "Fatal", "status": "healthy", "messages": [] }
            |  ]
            |}
            """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")
      }
    }

    it(
      "should return correct healthy response when some healthchecks are unhealthy but those are all NonFatal."
    ) {
      val ok1 = healthCheck("test1")(healthy)
      val failedButNonFatal =
        healthCheck("test2", Severity.NonFatal)(unhealthy("error"))

      Get("/health") ~> HealthCheckRoutes.health(ok1, failedButNonFatal) ~> check {
        status shouldEqual OK
        responseAs[String] shouldEqual "{}"
      }

      Get("/health?full=true") ~> HealthCheckRoutes.health(ok1, failedButNonFatal) ~> check {
        status shouldEqual OK
        responseAs[String] shouldEqual
          """
            |{
            |  "status": "healthy",
            |  "check_results": [
            |    { "name": "test1", "severity": "Fatal",    "status": "healthy",   "messages": [] },
            |    { "name": "test2", "severity": "NonFatal", "status": "unhealthy", "messages": ["error"] }
            |  ]
            |}
            """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")
      }
    }

    it(
      "should return correct response when some of 'Fatal' healthchecks are unhealthy system with a fatal error"
    ) {
      val ok = healthCheck("test1")(healthy)
      val failedButNonFatal =
        healthCheck("test2", Severity.NonFatal)(unhealthy("error"))
      val failedFatal = healthCheck("test3")(throw new Exception("exception"))

      Get("/health") ~> HealthCheckRoutes.health(ok, failedButNonFatal, failedFatal) ~> check {
        status shouldEqual ServiceUnavailable
        responseAs[String] shouldEqual "{}"
      }

      Get("/health?full=true") ~> HealthCheckRoutes.health(ok, failedButNonFatal, failedFatal) ~> check {
        status shouldEqual ServiceUnavailable
        responseAs[String] shouldEqual
          """
            |{
            |  "status": "unhealthy",
            |  "check_results": [
            |    { "name": "test1", "severity": "Fatal",    "status": "healthy",   "messages": [] },
            |    { "name": "test2", "severity": "NonFatal", "status": "unhealthy", "messages": ["error"] },
            |    { "name": "test3", "severity": "Fatal",    "status": "unhealthy", "messages": ["exception"] }
            |  ]
            |}
            """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")
      }
    }
  }

}
