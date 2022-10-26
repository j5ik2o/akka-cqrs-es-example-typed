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
package com.github.j5ik2o.adceet.api.write.adaptor.http.controller

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers.segment
import akka.http.javadsl.server.Route
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMemberRequestJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMemberResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMessageRequestJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.AddMessageResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadRequestJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationRejection
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.Validator
import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.MessageId
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import com.github.j5ik2o.adceet.api.write.use.case.AddMemberUseCase
import com.github.j5ik2o.adceet.api.write.use.case.AddMessageUseCase
import com.github.j5ik2o.adceet.api.write.use.case.CreateThreadUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType

class ThreadController(
  private val createThreadUseCase: CreateThreadUseCase,
  private val addMemberUseCase: AddMemberUseCase,
  private val addMessageUseCase: AddMessageUseCase
) : AllDirectives() {

  fun toRoute(): Route {
    return concat(createThread(), addMember(), addMessage())
  }

  @Path("/threads")
  @POST
  @Operation(
    description = "Create a thread",
    method = "POST",
    requestBody = RequestBody(
      required = true,
      content = [
        Content(
          schema = Schema(implementation = CreateThreadRequestJson::class),
          mediaType = MediaType.APPLICATION_JSON
        )
      ],
    ),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
          Content(
            schema = Schema(implementation = CreateThreadResponseJson::class),
            mediaType = MediaType.APPLICATION_JSON
          )
        ],
      )
    ]
  )
  fun createThread(): Route {
    return path("threads") {
      post {
        entity(
          Jackson.unmarshaller(
            JacksonObjectMappers.default,
            CreateThreadRequestJson::class.java
          )
        ) { json ->
          Validator.validateAccountId(json.accountId).fold(
            { errors ->
              reject(ValidationRejection(errors))
            },
            { accountId ->
              onSuccess(createThreadUseCase.execute(ThreadId(), accountId)) { threadId ->
                complete(
                  StatusCodes.OK,
                  CreateThreadResponseJson(threadId.asString()),
                  Jackson.marshaller(JacksonObjectMappers.default)
                )
              }
            }
          )
        }
      }
    }
  }

  @Path("/threads/{threadId}/member")
  @POST
  @Operation(
    description = "Add a member to the thread",
    method = "POST",
    parameters = [Parameter(name = "threadId", `in` = ParameterIn.PATH)],
    requestBody = RequestBody(
      description = "add member request",
      required = true,
      content = [
        Content(
          schema = Schema(implementation = AddMemberRequestJson::class),
          mediaType = MediaType.APPLICATION_JSON
        )
      ],
    ),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
          Content(
            schema = Schema(implementation = AddMemberRequestJson::class),
            mediaType = MediaType.APPLICATION_JSON
          )
        ],
      )
    ]
  )
  fun addMember(): Route {
    return path(segment("threads").slash(segment()).slash("members")) { threadIdString ->
      post {
        entity(Jackson.unmarshaller(JacksonObjectMappers.default, AddMemberRequestJson::class.java)) { json ->
          Validator.validateThreadIdWithAccountId(threadIdString, json.accountId).fold(
            { errors ->
              reject(ValidationRejection(errors))
            },
            { (threadId, accountId) ->
              val result = addMemberUseCase.execute(threadId, accountId)
              onSuccess(result) {
                complete(
                  StatusCodes.OK,
                  AddMemberResponseJson(threadId.asString()),
                  Jackson.marshaller(JacksonObjectMappers.default)
                )
              }
            }
          )
        }
      }
    }
  }

  @Path("/threads/{threadId}/messages")
  @POST
  @Operation(
    description = "Add a message to the thread",
    method = "POST",
    parameters = [Parameter(name = "threadId", `in` = ParameterIn.PATH)],
    requestBody = RequestBody(
      description = "add message request",
      required = true,
      content = [
        Content(
          schema = Schema(implementation = AddMessageRequestJson::class),
          mediaType = MediaType.APPLICATION_JSON
        )
      ],
    ),
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "OK",
        content = [
          Content(
            schema = Schema(implementation = AddMessageResponseJson::class),
            mediaType = MediaType.APPLICATION_JSON
          )
        ],
      )
    ]
  )
  fun addMessage(): Route {
    return path(segment("threads").slash(segment()).slash("messages")) { threadIdString ->
      post {
        entity(Jackson.unmarshaller(JacksonObjectMappers.default, AddMessageRequestJson::class.java)) { json ->
          Validator.validateThreadIdWithAccountId(threadIdString, json.accountId).fold(
            { errors ->
              reject(ValidationRejection(errors))
            },
            { (threadId, accountId) ->
              val message = Message(MessageId(), threadId, accountId, json.body)
              val result = addMessageUseCase.execute(message)
              onSuccess(result) {
                complete(
                  StatusCodes.OK,
                  AddMessageResponseJson(threadId.asString(), accountId.asString()),
                  Jackson.marshaller(JacksonObjectMappers.default)
                )
              }
            }
          )
        }
      }
    }
  }
}
