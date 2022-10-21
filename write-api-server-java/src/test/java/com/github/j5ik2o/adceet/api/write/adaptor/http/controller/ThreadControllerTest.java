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
package com.github.j5ik2o.adceet.api.write.adaptor.http.controller;

import static org.mockito.Mockito.*;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMemberRequestJson;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMessageRequestJson;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMessageResponseJson;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadRequestJson;
import com.github.j5ik2o.adceet.api.write.domain.AccountId;
import com.github.j5ik2o.adceet.api.write.domain.Message;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.use_case.AddMemberUseCase;
import com.github.j5ik2o.adceet.api.write.use_case.AddMessageUseCase;
import com.github.j5ik2o.adceet.api.write.use_case.CreateThreadUseCase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.*;
import org.mockito.stubbing.Answer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThreadControllerTest {
  private static Config CONFIG = ConfigFactory.empty();
  static final RouteTestHelper routerTestHelper = new RouteTestHelper(CONFIG);

  static CreateThreadUseCase mockCreateThreadUseCase = mock(CreateThreadUseCase.class);
  static AddMemberUseCase mockAddMemberUseCase = mock(AddMemberUseCase.class);
  static AddMessageUseCase mockAddMessageUseCase = mock(AddMessageUseCase.class);

  @BeforeAll
  void beforeTest() {
    routerTestHelper.setUp();
  }

  @AfterAll
  void afterTest() {
    routerTestHelper.tearDown();
  }

  @Test
  void スレッドを作成できる() throws JsonProcessingException {
    var accountId = new AccountId();
    when(mockCreateThreadUseCase.execute(any(ThreadId.class), eq(accountId)))
        .thenAnswer(
            (Answer<CompletionStage<ThreadId>>)
                invocation -> {
                  var threadId = (ThreadId) invocation.getArgument(0);
                  return CompletableFuture.completedFuture(threadId);
                });

    var controllerRoute =
        routerTestHelper.testRoute(
            new ThreadController(
                    mockCreateThreadUseCase, mockAddMemberUseCase, mockAddMessageUseCase)
                .createThread());

    var jsonAsString =
        JacksonObjectMappers.defaultObjectMapper()
            .writeValueAsString(new CreateThreadRequestJson(accountId.asString()));
    var entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString);

    var createThreadResult = controllerRoute.run(HttpRequest.POST("/threads").withEntity(entity));
    Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(createThreadResult.statusCode()));

    verify(mockCreateThreadUseCase, times(1)).execute(any(ThreadId.class), eq(accountId));
  }

  @Test
  void メンバーを追加できる() throws JsonProcessingException {
    var accountId = new AccountId();
    var threadId = new ThreadId();

    when(mockAddMemberUseCase.execute(eq(threadId), eq(accountId)))
        .thenAnswer(
            (Answer<CompletionStage<ThreadId>>)
                invocation -> {
                  var threadId1 = (ThreadId) invocation.getArgument(0);
                  return CompletableFuture.completedFuture(threadId1);
                });

    var controllerRoute =
        routerTestHelper.testRoute(
            new ThreadController(
                    mockCreateThreadUseCase, mockAddMemberUseCase, mockAddMessageUseCase)
                .addMember());

    var jsonAsString =
        JacksonObjectMappers.defaultObjectMapper()
            .writeValueAsString(new AddMemberRequestJson(accountId.asString()));
    var entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString);

    var createThreadResult =
        controllerRoute.run(
            HttpRequest.POST("/threads/%s/members".formatted(threadId.asString()))
                .withEntity(entity));
    Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(createThreadResult.statusCode()));

    verify(mockAddMemberUseCase, times(1)).execute(eq(threadId), eq(accountId));
  }

  @Test
  void メッセージを追加できる() throws JsonProcessingException {
    var accountId = new AccountId();
    var threadId = new ThreadId();
    var body = "ABC";

    when(mockAddMessageUseCase.execute(any(Message.class)))
        .thenAnswer(
            (Answer<CompletionStage<ThreadId>>)
                invocation -> {
                  var message = (Message) invocation.getArgument(0);
                  return CompletableFuture.completedFuture(message.threadId());
                });

    var controllerRoute =
        routerTestHelper.testRoute(
            new ThreadController(
                    mockCreateThreadUseCase, mockAddMemberUseCase, mockAddMessageUseCase)
                .addMessage());

    var jsonAsString =
        JacksonObjectMappers.defaultObjectMapper()
            .writeValueAsString(new AddMessageRequestJson(accountId.asString(), body));

    var entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString);

    var addMessageResult =
        controllerRoute.run(
            HttpRequest.POST("/threads/%s/messages".formatted(threadId.asString()))
                .withEntity(entity));

    Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(addMessageResult.statusCode()));
    var responseJson =
        addMessageResult.entity(
            Jackson.unmarshaller(
                JacksonObjectMappers.defaultObjectMapper(), AddMessageResponseJson.class));
    Assertions.assertEquals(threadId.asString(), responseJson.threadId());

    //    verify { mockCreateThreadUseCase.execute(threadId, accountId) wasNot Called }
    //    verify { mockAddMemberUseCase.execute(threadId, accountId) wasNot Called }
    //    verify { mockAddMessageUseCase.execute(messageSlot.captured) }

    //    confirmVerified(mockCreateThreadUseCase)
    //    confirmVerified(mockAddMemberUseCase)
    //    confirmVerified(mockAddMessageUseCase)
  }
}
