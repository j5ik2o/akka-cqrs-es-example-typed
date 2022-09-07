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

import akka.actor.ActorSystem;
import akka.actor.BootstrapSetup;
import akka.actor.setup.ActorSystemSetup;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.RouteResult;
import akka.http.javadsl.testkit.RouteTest;
import akka.http.javadsl.testkit.TestRouteResult;
import akka.serialization.jackson.JacksonObjectMapperFactory;
import akka.serialization.jackson.JacksonObjectMapperProviderSetup;
import akka.stream.Materializer;
import akka.stream.SystemMaterializer;
import com.typesafe.config.Config;
import org.junit.Assert;
import scala.concurrent.Future;

import java.util.function.Function;
import java.util.function.Supplier;

public class RouteTestHelper extends RouteTest {
  private final Config additionalConfig;
  private ActorTestKit testKit;

  private ActorTestKit createTestKit() {
    var setup =
        ActorSystemSetup.create(BootstrapSetup.create(additionalConfig))
            .withSetup(new JacksonObjectMapperProviderSetup(new JacksonObjectMapperFactory()));
    var system = akka.actor.typed.ActorSystem.create(Behaviors.empty(), "test-system", setup);
    return ActorTestKit.create(system);
  }

  public RouteTestHelper(Config additionalConfig) {
    super();
    this.additionalConfig = additionalConfig;
  }

  ActorTestKit testKit() {
    return testKit;
  }

  void setUp() {
    testKit = createTestKit();
  }

  void tearDown() {
    testKit.shutdownTestKit();
  }

  @Override
  public ActorSystem system() {
    return testKit.system().classicSystem();
  }

  @Override
  public Materializer materializer() {
    return SystemMaterializer.get(system()).materializer();
  }

  @Override
  public TestRouteResult createTestRouteResultAsync(
      HttpRequest request, Future<RouteResult> result) {
    return new TestRouteResult(result, awaitDuration(), system().dispatcher(), materializer()) {
      private <T> T reportDetails(Supplier<T> block) {
        try {
          return block.get();
        } catch (Throwable t) {
          throw new AssertionError(
              t.getMessage()
                  + "\n"
                  + "  Request was:      "
                  + request
                  + "\n"
                  + "  Route result was: "
                  + result
                  + "\n",
              t);
        }
      }

      @Override
      public void fail(String message) {
        Assert.fail(message);
        throw new IllegalStateException("Assertion should have failed");
      }

      @Override
      public void assertEquals(Object expected, Object actual, String message) {
        reportDetails(
            () -> {
              Assert.assertEquals(message, expected, actual);
              return null;
            });
      }

      @Override
      public void assertEquals(int expected, int actual, String message) {}

      @Override
      public void assertTrue(boolean predicate, String message) {
        Assert.assertTrue(message, predicate);
      }
    };
  }
}
