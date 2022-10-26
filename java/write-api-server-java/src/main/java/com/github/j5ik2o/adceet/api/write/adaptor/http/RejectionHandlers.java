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
package com.github.j5ik2o.adceet.api.write.adaptor.http;

import static akka.http.javadsl.server.Directives.complete;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.RejectionHandler;
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers;
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.ErrorsResponseJson;
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationRejection;
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.error.ValidationError;

public class RejectionHandlers {

  static RejectionHandler defaultHandler() {
    var builder = RejectionHandler.newBuilder();
    builder.handle(
        ValidationRejection.class,
        rejection -> {
          var messages = rejection.errorMessages().map(ValidationError::msg).toList().toJavaList();
          var responseJson = new ErrorsResponseJson(messages);
          return complete(
              StatusCodes.BAD_REQUEST,
              responseJson,
              Jackson.marshaller(JacksonObjectMappers.defaultObjectMapper()));
        });
    return builder.build();
  }
}
