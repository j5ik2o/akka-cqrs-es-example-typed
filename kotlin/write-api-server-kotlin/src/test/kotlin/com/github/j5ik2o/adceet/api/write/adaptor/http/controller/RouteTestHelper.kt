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

import akka.actor.ActorSystem
import akka.actor.BootstrapSetup
import akka.actor.setup.ActorSystemSetup
import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.javadsl.Behaviors
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.server.RouteResult
import akka.http.javadsl.testkit.RouteTest
import akka.http.javadsl.testkit.TestRouteResult
import akka.serialization.jackson.JacksonObjectMapperProviderSetup
import akka.stream.Materializer
import akka.stream.SystemMaterializer
import com.github.j5ik2o.adceet.api.write.KotlinModuleJacksonObjectMapperFactory
import com.typesafe.config.Config
import org.junit.Assert
import scala.concurrent.Future

class RouteTestHelper(private val additionalConfig: Config) : RouteTest() {

  private fun createTestKit(): ActorTestKit {
    val setup = ActorSystemSetup.create(BootstrapSetup.create(additionalConfig)).withSetup(
      JacksonObjectMapperProviderSetup(KotlinModuleJacksonObjectMapperFactory())
    )
    val system = akka.actor.typed.ActorSystem.create(
      Behaviors.empty<Any>(),
      "test-system",
      setup
    )
    return ActorTestKit.create(system)
  }

  private lateinit var testKit: ActorTestKit

  fun testKit(): ActorTestKit {
    return testKit
  }

  fun setUp() {
    testKit = createTestKit()
  }

  fun tearDown() {
    testKit.shutdownTestKit()
  }

  override fun system(): ActorSystem {
    return testKit.system().classicSystem()
  }

  override fun materializer(): Materializer {
    return SystemMaterializer.get(system()).materializer()
  }

  override fun createTestRouteResultAsync(request: HttpRequest?, result: Future<RouteResult>?): TestRouteResult {
    return object : TestRouteResult(result, awaitDuration(), system().dispatcher, materializer()) {
      fun <T> reportDetails(block: () -> T): T {
        return try {
          block()
        } catch (t: Throwable) {
          throw AssertionError(
            t.message + "\n" +
              "  Request was:      " + request + "\n" +
              "  Route result was: " + result + "\n",
            t
          )
        }
      }

      override fun fail(message: String?) {
        Assert.fail(message)
        throw IllegalStateException("Assertion should have failed")
      }

      override fun assertTrue(predicate: Boolean, message: String?) {
        Assert.assertTrue(message, predicate)
      }

      override fun assertEquals(expected: Any?, actual: Any?, message: String?) {
        reportDetails { Assert.assertEquals(message, expected, actual) }
      }

      override fun assertEquals(expected: Int, actual: Int, message: String?) {
        Assert.assertEquals(message, expected, actual)
      }
    }
  }
}
