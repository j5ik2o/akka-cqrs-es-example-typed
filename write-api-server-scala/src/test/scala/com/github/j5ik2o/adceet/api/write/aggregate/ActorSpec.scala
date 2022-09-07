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
package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.ActorSystem
import akka.actor.testkit.typed.TestKitSettings
import akka.actor.testkit.typed.scaladsl.{ ActorTestKit, ActorTestKitBase, ScalaTestWithActorTestKit }
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.time.{ Millis, Seconds, Span }

import scala.concurrent.duration.FiniteDuration

abstract class ActorSpec(testKit: ActorTestKit) extends ScalaTestWithActorTestKit(testKit) with AnyFreeSpecLike {

  def testTimeFactor: Double = testKit.testKitSettings.TestTimeFactor

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = scaled(Span(5 * testTimeFactor, Seconds)),
      interval = scaled(Span(15 * testTimeFactor, Millis))
    )

  def this() = this(ActorTestKit(ActorTestKitBase.testNameFromCallStack()))

  def this(config: String) =
    this(
      ActorTestKit(
        ActorTestKitBase.testNameFromCallStack(),
        ConfigFactory.parseString(config)
      )
    )

  def this(config: Config) =
    this(ActorTestKit(ActorTestKitBase.testNameFromCallStack(), config))

  def this(config: Config, settings: TestKitSettings) =
    this(
      ActorTestKit(
        ActorTestKitBase.testNameFromCallStack(),
        config,
        settings
      )
    )

  implicit def classicSystem: ActorSystem = system.toClassic

  def killActors(actors: ActorRef[_]*)(maxDuration: FiniteDuration = timeout.duration): Unit = {
    actors.foreach { actorRef => testKit.stop(actorRef, maxDuration) }
  }

}
