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
package com.github.j5ik2o.adceet.api.read.adaptor.http.controller

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.j5ik2o.adceet.api.read.adaptor.http.json.{MessageJson, ThreadJson}
import com.github.j5ik2o.adceet.api.read.adaptor.http.validation.{AccountId, ThreadId}
import com.github.j5ik2o.adceet.api.read.use.`case`.GetThreadsUseCase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import io.circe.generic.auto._

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class ThreadControllerSpec
    extends AnyFreeSpec
    with Matchers
    with ScalatestRouteTest
    with FailFastCirceSupport
    with MockitoSugar
    with ArgumentMatchersSugar
    with HttpTestSupport {

  "ThreadController" - {
    "getThreads" in {
      val getThreadsInteractor = mock[GetThreadsUseCase]
      val threadController = new ThreadController(getThreadsInteractor)

      val accountId = AccountId()
      val threadId = ThreadId()

      when(getThreadsInteractor.execute(accountId)).thenAnswer { accountId: AccountId =>
        Future.successful(
          Seq(getThreadsInteractor.ThreadRecord(threadId.asString, accountId.asString, Instant.now))
        )
      }

      Get(s"/threads?owner_id=${accountId.asString}") ~> threadController.toRoute ~> check {
        val response = responseAs[Seq[ThreadJson]]
        response.size shouldBe 1
        response.head.id shouldBe threadId.asString
      }
    }
  }



}
