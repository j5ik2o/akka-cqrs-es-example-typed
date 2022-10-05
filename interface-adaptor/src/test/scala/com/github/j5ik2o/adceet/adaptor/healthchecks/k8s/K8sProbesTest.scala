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
package com.github.j5ik2o.adceet.adaptor.healthchecks.k8s

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.{asyncHealthCheck, healthCheck, healthy}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class K8sProbesTest extends AnyFreeSpec with Matchers {
  private def fixture(probe: K8sProbe, probes: K8sProbe*) = new {}

  "K8sProbes" - {
    "should start successfully and return correct response" in {
      implicit val system = ActorSystem()
      implicit val ec     = system.dispatcher

      val _ = bindAndHandleProbes(
        readinessProbe(healthCheck("readiness_check")(healthy)),
        livenessProbe(asyncHealthCheck("liveness_check")(Future(healthy)))
      )

      def requestToReadinessProbe: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8086/ready"))
      def requestToLivenessProbe: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8086/live"))

      val readinessResponse = Await.result(requestToReadinessProbe, 10.seconds)
      val livenessResponse = Await.result(requestToLivenessProbe, 10.seconds)

      readinessResponse.status shouldEqual StatusCodes.OK
      livenessResponse.status shouldEqual StatusCodes.OK

      system.terminate()
    }
  }
}
