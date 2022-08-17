package com.github.j5ik2o.adceet.api.write

import enumeratum._
import akka.actor.typed.ActorSystem
import wvlet.airframe.{ DISupport, Session }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

sealed trait Environment extends EnumEntry

object Environments extends Enum[Environment] {
  case object Development extends Environment
  case object Production extends Environment

  override def values: IndexedSeq[Environment] = findValues

  implicit val weekDaysRead: scopt.Read[Environment] =
    scopt.Read.reads(Environments.withNameInsensitive)

}

final case class Args(environment: Environment = Environments.Production)

object Main extends App with DISupport {
  import Environments._
  import scopt._

  val builder = OParser.builder[Args]
  val parser = {
    import builder._
    OParser.sequence(
      programName("write-api-server"),
      opt[Environment]('e', "env")
        .text("Environment value")
        .action { (x, c) =>
          c.copy(environment = x)
        }
    )
  }

  val parsedArgs = OParser.parse(parser, args, Args()).get

  val design           = DISettings.di(parsedArgs)
  val session: Session = design.newSession
  try {
    val system = session.build[ActorSystem[MainActor.Command]]
    Await.result(system.whenTerminated, Duration.Inf)
  } finally {
    session.shutdown
  }
}
