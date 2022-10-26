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

import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController;
import scala.collection.immutable.Set;

public class Routes extends AllDirectives {
  private final ThreadController threadController;

  public Routes(ThreadController threadController) {
    super();
    this.threadController = threadController;
  }

  public Route toRoute() {
    return handleExceptions(
        ExceptionHandlers.defaultHandler(),
        () ->
            handleRejections(
                RejectionHandlers.defaultHandler(),
                () -> {
                  var swaggerDocServiceRoute =
                      new RouteAdapter(
                          new SwaggerDocService(
                                  "127.0.0.1", 8081, new Set.Set1(ThreadController.class))
                              .toRoute());
                  return concat(hello(), swaggerDocServiceRoute, threadController.toRoute());
                }));
  }

  private Route hello() {
    return path("hello", () -> get(() -> complete("Say hello to akka-http")));
  }
}
