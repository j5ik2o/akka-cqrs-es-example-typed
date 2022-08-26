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
