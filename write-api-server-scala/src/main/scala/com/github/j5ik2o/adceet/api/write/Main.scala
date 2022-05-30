package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.ActorSystem
import wvlet.airframe.{ DISupport, Session }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App with DISupport {
  val design           = DISettings.di
  val session: Session = design.newSession
  try {
    val system = session.build[ActorSystem[MainActor.Command]]
    Await.result(system.whenTerminated, Duration.Inf)
  } finally {
    session.shutdown
  }
}
