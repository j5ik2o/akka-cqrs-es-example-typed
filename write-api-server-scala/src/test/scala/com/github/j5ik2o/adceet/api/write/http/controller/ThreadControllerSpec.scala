package com.github.j5ik2o.adceet.api.write.http.controller

import akka.http.scaladsl.model.{ HttpEntity, MediaTypes, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.github.j5ik2o.adceet.api.write.domain.{ AccountId, Message, ThreadId }
import com.github.j5ik2o.adceet.api.write.http.json._
import com.github.j5ik2o.adceet.api.write.use.`case`.{ AddMemberUseCase, AddMessageUseCase, CreateThreadUseCase }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.mockito.captor.ArgCaptor
import org.mockito.{ ArgumentMatchersSugar, MockitoSugar }
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
