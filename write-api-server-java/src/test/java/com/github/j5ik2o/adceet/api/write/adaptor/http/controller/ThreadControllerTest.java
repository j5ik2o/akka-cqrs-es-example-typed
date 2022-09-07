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

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadRequestJson;
import com.github.j5ik2o.adceet.api.write.domain.AccountId;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.use_case.AddMemberUseCase;
import com.github.j5ik2o.adceet.api.write.use_case.CreateThreadUseCase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThreadControllerTest {
    private static Config CONFIG = ConfigFactory.empty();
    static final RouteTestHelper routerTestHelper = new RouteTestHelper(CONFIG);

    static CreateThreadUseCase mockCreateThreadUseCase = mock(CreateThreadUseCase.class);
    static AddMemberUseCase mockAddMemberUseCase = mock(AddMemberUseCase.class);
//    AddMessageUseCase mockAddMessageUseCase  = mockk()

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
        when(mockCreateThreadUseCase.execute(any(ThreadId.class), eq(accountId))).thenAnswer(new Answer<CompletionStage<ThreadId>>() {
            @Override
            public CompletionStage<ThreadId> answer(InvocationOnMock invocation) throws Throwable {
                var threadId = (ThreadId)invocation.getArgument(0);
                return CompletableFuture.completedFuture(threadId);
            }
        });

        var controllerRoute = routerTestHelper.testRoute(
                new ThreadController(
                        mockCreateThreadUseCase
                      //  mockAddMemberUseCase,
                       // mockAddMessageUseCase
                ).createThread()
        );

        var jsonAsString =
                JacksonObjectMappers.defaultObjectMapper().writeValueAsString(new CreateThreadRequestJson(accountId.asString()));
        var entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, jsonAsString);

        var createThreadResult = controllerRoute.run(HttpRequest.POST("/threads").withEntity(entity));
        Assertions.assertEquals(StatusCodes.OK, StatusCodes.get(createThreadResult.statusCode()));

        verify(mockCreateThreadUseCase, times(1)).execute(any(ThreadId.class), eq(accountId));

    }
}