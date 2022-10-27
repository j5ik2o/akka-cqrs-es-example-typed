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
package com.github.j5ik2o.adceet.api.read

import akka.Done
import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.github.j5ik2o.adceet.api.read.MainActor.Command
import com.github.j5ik2o.adceet.api.read.adaptor.http.Routes
import com.github.j5ik2o.adceet.api.read.adaptor.http.controller.{MemberController, MessageController, ThreadController}
import kamon.Kamon
import org.slf4j.LoggerFactory
import wvlet.airframe.Session
import wvlet.log.io.StopWatch

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.util.{Failure, Success}
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.{asyncHealthCheck, healthCheck, healthy}
import com.github.j5ik2o.adceet.adaptor.healthchecks.k8s.{livenessProbe, readinessProbe}
import akka.http.scaladsl.server.Directives._

object MainActor {
  sealed trait Command
}

class MainActor(val session: Session, stopWatch: StopWatch) {
  val logger = LoggerFactory.getLogger(getClass)

  private def startHttpServer(
                               session: Session,
                               system: ActorSystem[_],
                               host: String,
                               port: Int,
                               terminationHardDeadLine: FiniteDuration
                             )(implicit classic: ClassicActorSystemProvider, ec: ExecutionContext): Future[Done] = {
    logger.info(s"[${stopWatch.reportElapsedTime}] startHttpServer: start")

    val threadController: ThreadController = session.build[ThreadController]
    val memberController: MemberController = session.build[MemberController]
    val messageController: MessageController = session.build[MessageController]

    implicit val s = system.classicSystem

    val route = concat(
      new Routes(threadController, memberController, messageController).toRoute,
      readinessProbe(healthCheck("readiness_check")(healthy)).toRoute,
      livenessProbe(asyncHealthCheck("liveness_check")(Future(healthy))).toRoute
    )

    val http = Http()
      .newServerAt(host, port)
      .bind(route)
      .map {
        _.addToCoordinatedShutdown(terminationHardDeadLine)
      }
    http.onComplete {
      case Success(serverBinding) =>
        val address = serverBinding.localAddress
        logger.info(
          s"[${stopWatch.reportElapsedTime}] server bound to http://${address.getHostString}:${address.getPort}"
        )
      case Failure(ex) =>
        logger.error(s"[${stopWatch.reportElapsedTime}] Failed to bind endpoint, terminating system: $ex")
        system.terminate()
    }
    val result = http.map(_ => Done)
    logger.info(s"[${stopWatch.reportElapsedTime}] startHttpServer: finish")
    result
  }
  def create(args: Args): Behavior[Command] = {
    if (args.environment == Environments.Production)
      Kamon.init()

    Behaviors.setup { ctx =>
      implicit val system: ActorSystem[_] = ctx.system
      implicit val ec: ExecutionContextExecutor = ctx.executionContext

      val config = ctx.system.settings.config
      val adceetConfig = config.getConfig("adceet")
      val host = adceetConfig.getString("http.host")
      val port = adceetConfig.getInt("http.port")
      val terminationHardDeadLine =
        adceetConfig.getDuration("http.termination-hard-deadline").toMillis.millis

      val future = startHttpServer(session, ctx.system, host, port, terminationHardDeadLine)

      Await.result(future, 10.seconds)

      Behaviors.receiveMessage {
        case _ =>
          Behaviors.same
      }
    }
  }
}