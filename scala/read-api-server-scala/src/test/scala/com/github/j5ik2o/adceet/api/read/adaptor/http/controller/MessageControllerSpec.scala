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
import com.github.j5ik2o.adceet.api.read.adaptor.http.json.MessageJson
import com.github.j5ik2o.adceet.api.read.adaptor.http.validation.ThreadId
import com.github.j5ik2o.adceet.api.read.use.`case`.GetMessagesUseCase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import wvlet.airframe.ulid.ULID

import java.time.Instant
import scala.concurrent.Future

class MessageControllerSpec
    extends AnyFreeSpec
    with Matchers
    with ScalatestRouteTest
    with FailFastCirceSupport
    with MockitoSugar
    with ArgumentMatchersSugar
    with HttpTestSupport {

  "MessageController" - {
    "getMessages" in {
      val getMessagesInteractor = mock[GetMessagesUseCase]
      val messageController     = new MessageController(getMessagesInteractor)

      val accountId = ULID.newULIDString
      val threadId  = ThreadId()

      when(getMessagesInteractor.execute(threadId)).thenAnswer { threadId: ThreadId =>
        val id = ULID.newULIDString
        Future.successful(
          Seq(getMessagesInteractor.MessageRecord(id, threadId.asString, accountId, "test", Instant.now))
        )
      }

      Get(s"/messages?thread_id=${threadId.asString}") ~> messageController.toRoute ~> check {
        val response = responseAs[Seq[MessageJson]]
        response.size shouldBe 1
        response.head.thread_id shouldBe threadId.asString
      }
    }
  }

}
