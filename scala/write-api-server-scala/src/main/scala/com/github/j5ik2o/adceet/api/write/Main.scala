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
package com.github.j5ik2o.adceet.api.write

import enumeratum._
import akka.actor.typed.ActorSystem
import org.slf4j.LoggerFactory
import wvlet.airframe.{ DISupport, DesignWithContext, Session }
import wvlet.log.io.StopWatch

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.slf4j.Logger

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
  val stopWatch = new StopWatch()

  import Environments._
  import scopt._

  val logger: Logger = LoggerFactory.getLogger(getClass)

  logger.info(s"[${stopWatch.reportElapsedTime}] start")

  val builder: OParserBuilder[Args] = OParser.builder[Args]
  val parser: OParser[Unit, Args] = {
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

  val parsedArgs: Args = OParser.parse(parser, args, Args()).get

  val design: DesignWithContext[_] = DISettings.di(parsedArgs, stopWatch)
  val session: Session             = design.newSession
  try {
    val system = session.build[ActorSystem[MainActor.Command]]
    Await.result(system.whenTerminated, Duration.Inf)
  } finally {
    session.shutdown
    logger.info("finish")
  }
}
