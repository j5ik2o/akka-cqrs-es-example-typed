package com.github.j5ik2o.adceet.api.write.adaptor.http.controller

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.ContentTypes
import akka.http.javadsl.model.HttpEntities
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.StatusCodes
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMemberRequestJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMemberResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMessageRequestJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMessageResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadRequestJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationError
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationRejection
import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.MessageId
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import com.github.j5ik2o.adceet.api.write.use.case.AddMemberUseCase
import com.github.j5ik2o.adceet.api.write.use.case.AddMessageUseCase
import com.github.j5ik2o.adceet.api.write.use.case.CreateThreadUseCase
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.CompletableFuture

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThreadControllerTest {
  companion object {
    private val CONFIG: Config = ConfigFactory.empty()
    val routerTestHelper = RouteTestHelper(CONFIG)

    val mockCreateThreadUseCase: CreateThreadUseCase = mockk()
    val mockAddMemberUseCase: AddMemberUseCase = mockk()
    val mockAddMessageUseCase: AddMessageUseCase = mockk()
  }

  @BeforeAll
  fun beforeTest() {
    routerTestHelper.setUp()
  }

  @AfterAll
  fun afterTest() {
    routerTestHelper.tearDown()
  }

  @BeforeEach
  fun afterEach() {
    clearMocks(mockCreateThreadUseCase)
    clearMocks(mockAddMemberUseCase)
    clearMocks(mockAddMessageUseCase)
  }

  @Test
  fun スレッドを作成できる() {
    val accountId = AccountId()
    val threadIdSlot = slot<ThreadId>()

    every {
      mockCreateThreadUseCase.execute(capture(threadIdSlot), accountId)
    } answers {
      val actualThreadId = threadIdSlot.captured
      CompletableFuture.completedFuture(actualThreadId)
    }

    val controllerRoute = routerTestHelper.testRoute(
      ThreadController(
        mockCreateThreadUseCase,
        mockAddMemberUseCase,
        mockAddMessageUseCase
      ).createThread()
    )

    val jsonAsString =
      JacksonObjectMappers.default.writeValueAsString(CreateThreadRequestJson(accountId.value.toString()))
    val entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString)

    val createThreadResult = controllerRoute.run(HttpRequest.POST("/threads").withEntity(entity))

    Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(createThreadResult.statusCode()))
    val responseJson = createThreadResult.entity(
      Jackson.unmarshaller(
        JacksonObjectMappers.default,
        CreateThreadResponseJson::class.java
      )
    )
    Assertions.assertEquals(threadIdSlot.captured.asString(), responseJson.threadId)

    verify { mockCreateThreadUseCase.execute(threadIdSlot.captured, accountId) }
    verify { mockAddMemberUseCase.execute(threadIdSlot.captured, accountId) wasNot Called }
    verify { mockAddMessageUseCase.execute(any()) wasNot Called }

    confirmVerified(mockCreateThreadUseCase)
    confirmVerified(mockAddMemberUseCase)
    confirmVerified(mockAddMessageUseCase)
  }

  @Test
  fun メンバーを追加できる() {
    val accountId = AccountId()
    val threadId = ThreadId()

    every {
      mockAddMemberUseCase.execute(threadId, accountId)
    } answers {
      CompletableFuture.completedFuture(threadId)
    }

    val controllerRoute = routerTestHelper.testRoute(
      ThreadController(
        mockCreateThreadUseCase,
        mockAddMemberUseCase,
        mockAddMessageUseCase
      ).addMember()
    )

    val jsonAsString =
      JacksonObjectMappers.default.writeValueAsString(AddMemberRequestJson(accountId.value.toString()))
    val entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString)

    val addMemberResult =
      controllerRoute.run(HttpRequest.POST("/threads/${threadId.asString()}/members").withEntity(entity))

    Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(addMemberResult.statusCode()))
    val responseJson = addMemberResult.entity(
      Jackson.unmarshaller(
        JacksonObjectMappers.default,
        AddMemberResponseJson::class.java
      )
    )
    Assertions.assertEquals(threadId.asString(), responseJson.threadId)

    verify { mockCreateThreadUseCase.execute(threadId, accountId) wasNot Called }
    verify { mockAddMemberUseCase.execute(threadId, accountId) }
    verify { mockAddMessageUseCase.execute(any()) wasNot Called }

    confirmVerified(mockCreateThreadUseCase)
    confirmVerified(mockAddMemberUseCase)
    confirmVerified(mockAddMessageUseCase)
  }

  @Test
  fun メッセージを追加できる() {
    val accountId = AccountId()
    val threadId = ThreadId()
    val messageId = MessageId()
    val body = "ABC"
    val messageSlot = slot<Message>()

    every {
      mockAddMessageUseCase.execute(capture(messageSlot))
    } answers {
      CompletableFuture.completedFuture(threadId)
    }

    val controllerRoute = routerTestHelper.testRoute(
      ThreadController(
        mockCreateThreadUseCase,
        mockAddMemberUseCase,
        mockAddMessageUseCase
      ).addMessage()
    )

    val jsonAsString =
      JacksonObjectMappers.default.writeValueAsString(
        AddMessageRequestJson(
          threadId.asString(),
          accountId.asString(),
          body
        )
      )
    val entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString)

    val addMessageResult =
      controllerRoute.run(HttpRequest.POST("/threads/${threadId.asString()}/messages").withEntity(entity))

    Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(addMessageResult.statusCode()))
    val responseJson = addMessageResult.entity(
      Jackson.unmarshaller(
        JacksonObjectMappers.default,
        AddMessageResponseJson::class.java
      )
    )
    Assertions.assertEquals(threadId.asString(), responseJson.threadId)

    verify { mockCreateThreadUseCase.execute(threadId, accountId) wasNot Called }
    verify { mockAddMemberUseCase.execute(threadId, accountId) wasNot Called }
    verify { mockAddMessageUseCase.execute(messageSlot.captured) }

    confirmVerified(mockCreateThreadUseCase)
    confirmVerified(mockAddMemberUseCase)
    confirmVerified(mockAddMessageUseCase)
  }

  @Nested
  inner class アカウントIDが不正な場合 {
    @Test
    fun スレッドを作成できない() {
      val controllerRoute = routerTestHelper.testRoute(
        ThreadController(
          mockCreateThreadUseCase,
          mockAddMemberUseCase,
          mockAddMessageUseCase
        ).createThread()
      )

      val jsonAsString = JacksonObjectMappers.default.writeValueAsString(CreateThreadRequestJson("abc"))
      val entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString)

      val createThreadResult =
        controllerRoute.runWithRejections(HttpRequest.POST("/threads").withEntity(entity))

      val rejection = createThreadResult.rejections()[0] as ValidationRejection
      Assertions.assertEquals(ValidationError.ParseError::class, rejection.errorMessages.head::class)

      verify { mockCreateThreadUseCase.execute(any(), any()) wasNot Called }
      verify { mockAddMemberUseCase.execute(any(), any()) wasNot Called }

      confirmVerified(mockCreateThreadUseCase)
      confirmVerified(mockAddMemberUseCase)
    }
  }

  @Nested
  inner class スレッドIDが不正な場合 {
    @Test
    fun メンバーを追加できない() {
      val accountId = AccountId()

      val controllerRoute = routerTestHelper.testRoute(
        ThreadController(
          mockCreateThreadUseCase,
          mockAddMemberUseCase,
          mockAddMessageUseCase
        ).addMember()
      )

      val jsonAsString =
        JacksonObjectMappers.default.writeValueAsString(AddMemberRequestJson(accountId.value.toString()))
      val entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString)

      val addMemberResult =
        controllerRoute.runWithRejections(HttpRequest.POST("/threads/xxxxxx/members").withEntity(entity))

      val rejection = addMemberResult.rejections()[0] as ValidationRejection
      Assertions.assertEquals(ValidationError.ParseError::class, rejection.errorMessages.head::class)

      verify { mockCreateThreadUseCase.execute(any(), any()) wasNot Called }
      verify { mockAddMemberUseCase.execute(any(), any()) wasNot Called }

      confirmVerified(mockCreateThreadUseCase)
      confirmVerified(mockAddMemberUseCase)
      confirmVerified(mockAddMessageUseCase)
    }
  }
}
