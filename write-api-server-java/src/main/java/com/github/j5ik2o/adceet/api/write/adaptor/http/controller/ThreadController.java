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

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;

import static akka.http.javadsl.server.PathMatchers.segment;

import akka.http.javadsl.server.Route;
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadRequestJson;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.CreateThreadResponseJson;
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationRejection;
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.Validator;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.use_case.CreateThreadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

public final class ThreadController extends AllDirectives {
  private final CreateThreadUseCase createThreadUseCase;

  public ThreadController(CreateThreadUseCase createThreadUseCase) {
    this.createThreadUseCase = createThreadUseCase;
  }

  public Route toRoute() {
    return concat(createThread());
  }

  @Path("/threads")
  @POST
  @Operation(
      description = "Create a thread",
      method = "POST",
      requestBody =
          @RequestBody(
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = CreateThreadRequestJson.class),
                      mediaType = MediaType.APPLICATION_JSON)),
      responses =
          @ApiResponse(
              responseCode = "200",
              description = "OK",
              content =
                  @Content(
                      schema = @Schema(implementation = CreateThreadResponseJson.class),
                      mediaType = MediaType.APPLICATION_JSON)))
  public Route createThread() {
    return path(
        "threads",
        () ->
            post(
                () ->
                    entity(
                        Jackson.unmarshaller(
                            JacksonObjectMappers.defaultObjectMapper(),
                            CreateThreadRequestJson.class),
                        json ->
                            Validator.validateAccountId(json.accountId())
                                .fold(
                                    errors -> reject(new ValidationRejection(errors)),
                                    accountId ->
                                        onSuccess(
                                            createThreadUseCase.execute(new ThreadId(), accountId),
                                            threadId ->
                                                complete(
                                                    StatusCodes.OK,
                                                    new CreateThreadResponseJson(
                                                        threadId.asString()),
                                                    Jackson.marshaller(
                                                        JacksonObjectMappers
                                                            .defaultObjectMapper())))))));
  }
}
