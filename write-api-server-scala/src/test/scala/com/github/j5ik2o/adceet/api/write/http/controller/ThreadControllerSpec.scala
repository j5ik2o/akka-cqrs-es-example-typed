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
package com.github.j5ik2o.adceet.api.write.http.controller

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController
import com.github.j5ik2o.adceet.api.write.adaptor.http.json._
import com.github.j5ik2o.adceet.api.write.use.`case`.{AddMemberUseCase, AddMessageUseCase, CreateThreadUseCase}
import com.github.j5ik2o.adceet.domain.{AccountId, Message, ThreadId}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.mockito.captor.ArgCaptor
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class ThreadControllerSpec
    extends AnyFreeSpec
    with Matchers
    with ScalatestRouteTest
    with FailFastCirceSupport
    with MockitoSugar
    with ArgumentMatchersSugar {

  implicit class ToHttpEntityOps[A: Encoder](json: A) {

    def toHttpEntity: HttpEntity.Strict = {
      val jsonAsByteString = ByteString(json.asJson.noSpaces)
      HttpEntity(MediaTypes.`application/json`, jsonAsByteString)
    }

  }

  "ThreadController" - {
    "createThread" in {
      val mockCreateThreadUseCase: CreateThreadUseCase = mock[CreateThreadUseCase]
      val mockAddMemberUseCase: AddMemberUseCase       = mock[AddMemberUseCase]
      val mockAddMessageUseCase: AddMessageUseCase     = mock[AddMessageUseCase]

      val controller = new ThreadController(
        mockCreateThreadUseCase,
        mockAddMemberUseCase,
        mockAddMessageUseCase
      )

      val threadIdCaptor = ArgCaptor[ThreadId]
      when(mockCreateThreadUseCase.execute(threadIdCaptor, any)(any)).thenAnswer { (threadId: ThreadId, _: AccountId) =>
        Future.successful(threadId)
      }

      val accountId = AccountId()
      val json      = CreateThreadRequestJson(accountId.asString)

      Post("/threads", json) ~> controller.createThread ~> check {
        response.status shouldEqual StatusCodes.OK
        val responseJson = responseAs[CreateThreadResponseJson]
        assert(responseJson.threadId == threadIdCaptor.value.asString)
      }

      verify(mockCreateThreadUseCase, times(1)).execute(any, any)(any)

    }
    "addMember" in {
      val mockCreateThreadUseCase: CreateThreadUseCase = mock[CreateThreadUseCase]
      val mockAddMemberUseCase: AddMemberUseCase       = mock[AddMemberUseCase]
      val mockAddMessageUseCase: AddMessageUseCase     = mock[AddMessageUseCase]

      val controller = new ThreadController(
        mockCreateThreadUseCase,
        mockAddMemberUseCase,
        mockAddMessageUseCase
      )

      val threadIdCaptorInCreateThread = ArgCaptor[ThreadId]
      when(mockCreateThreadUseCase.execute(threadIdCaptorInCreateThread, any)(any)).thenAnswer {
        (threadId: ThreadId, _: AccountId) =>
          Future.successful(threadId)
      }

      val threadIdCaptorInAddMember = ArgCaptor[ThreadId]
      when(mockAddMemberUseCase.execute(threadIdCaptorInAddMember, any)(any)).thenAnswer {
        (threadId: ThreadId, _: AccountId) =>
          Future.successful(threadId)
      }

      val accountId               = AccountId()
      val createThreadRequestJson = CreateThreadRequestJson(accountId.asString)

      Post("/threads", createThreadRequestJson) ~> controller.createThread ~> check {
        response.status shouldEqual StatusCodes.OK
        val responseJson = responseAs[CreateThreadResponseJson]
        assert(responseJson.threadId == threadIdCaptorInCreateThread.value.asString)
      }

      val addMemberRequestJson = AddMemberRequestJson(accountId.asString)

      Post(
        s"/threads/${threadIdCaptorInCreateThread.value.asString}/members",
        addMemberRequestJson
      ) ~> controller.addMember ~> check {
        response.status shouldEqual StatusCodes.OK
        val responseJson = responseAs[AddMemberResponseJson]
        assert(responseJson.threadId == threadIdCaptorInAddMember.value.asString)
      }

      verify(mockCreateThreadUseCase, times(1)).execute(any, any)(any)
      verify(mockAddMemberUseCase, times(1)).execute(any, any)(any)
    }
    "addMessage" in {
      val mockCreateThreadUseCase: CreateThreadUseCase = mock[CreateThreadUseCase]
      val mockAddMemberUseCase: AddMemberUseCase       = mock[AddMemberUseCase]
      val mockAddMessageUseCase: AddMessageUseCase     = mock[AddMessageUseCase]

      val controller = new ThreadController(
        mockCreateThreadUseCase,
        mockAddMemberUseCase,
        mockAddMessageUseCase
      )

      val threadIdCaptorInCreateThread = ArgCaptor[ThreadId]
      when(mockCreateThreadUseCase.execute(threadIdCaptorInCreateThread, any)(any)).thenAnswer {
        (threadId: ThreadId, _: AccountId) =>
          Future.successful(threadId)
      }

      val threadIdCaptorInAddMember = ArgCaptor[ThreadId]
      when(mockAddMemberUseCase.execute(threadIdCaptorInAddMember, any)(any)).thenAnswer {
        (threadId: ThreadId, _: AccountId) =>
          Future.successful(threadId)
      }
      val messageCaptorInAddMessage = ArgCaptor[Message]
      when(mockAddMessageUseCase.execute(messageCaptorInAddMessage)(any)).thenAnswer { (message: Message) =>
        Future.successful(message.threadId)
      }

      val accountId               = AccountId()
      val createThreadRequestJson = CreateThreadRequestJson(accountId.asString)

      Post("/threads", createThreadRequestJson) ~> controller.createThread ~> check {
        response.status shouldEqual StatusCodes.OK
        val responseJson = responseAs[CreateThreadResponseJson]
        assert(responseJson.threadId == threadIdCaptorInCreateThread.value.asString)
      }

      val addMemberRequestJson = AddMemberRequestJson(accountId.asString)

      Post(
        s"/threads/${threadIdCaptorInCreateThread.value.asString}/members",
        addMemberRequestJson
      ) ~> controller.addMember ~> check {
        response.status shouldEqual StatusCodes.OK
        val responseJson = responseAs[AddMemberResponseJson]
        assert(responseJson.threadId == threadIdCaptorInAddMember.value.asString)
      }

      val message               = "abc"
      val addMessageRequestJson = AddMessageRequestJson(accountId.asString, message)

      Post(
        s"/threads/${threadIdCaptorInAddMember.value.asString}/messages",
        addMessageRequestJson
      ) ~> controller.addMessage ~> check {
        response.status shouldEqual StatusCodes.OK
        val responseJson = responseAs[AddMessageResponseJson]
        assert(responseJson.threadId == messageCaptorInAddMessage.value.threadId.asString)
        assert(messageCaptorInAddMessage.value.threadId == threadIdCaptorInAddMember.value)
        assert(messageCaptorInAddMessage.value.senderId == accountId)
        assert(messageCaptorInAddMessage.value.body == message)
      }

      verify(mockCreateThreadUseCase, times(1)).execute(any, any)(any)
      verify(mockAddMemberUseCase, times(1)).execute(any, any)(any)
      verify(mockAddMessageUseCase, times(1)).execute(any)(any)

    }
  }
}
