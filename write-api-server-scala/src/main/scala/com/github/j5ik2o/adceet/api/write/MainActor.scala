package com.github.j5ik2o.adceet.api.write

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.actor.{ ClassicActorSystemProvider, CoordinatedShutdown }
import akka.cluster.typed.SelfUp
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.github.j5ik2o.adceet.api.write.http.Routes
import com.github.j5ik2o.adceet.api.write.http.controller.ThreadController
import org.slf4j.{ Logger, LoggerFactory }
import wvlet.airframe.Session

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object MainActor {
  sealed trait Command
  case object MeUp extends Command

  private val logger: Logger = LoggerFactory.getLogger(classOf[MainActor.type])

  def create(session: Session): Behavior[Command] = {
    Behaviors.setup { ctx =>
      val config                  = ctx.system.settings.config
      val host                    = config.getString("http.host")
      val port                    = config.getInt("http.port")
      val terminationHardDeadLine = config.getDuration("management.http.termination-hard-deadline").toMillis.millis
      val loadBalancerDetachWaitDuration =
        config.getDuration("management.http.load-balancer-detach-wait-duration").toMillis.millis

      val childSession = session.newChildSession(DISettings.mainDi(ctx))

      implicit val system = ctx.system
      implicit val ec     = ctx.executionContext

      val future = for {
        _ <- startApplicationServer(childSession, ctx.system, host, port, terminationHardDeadLine)
        _ <- startHealthCheckServer(ctx.system, loadBalancerDetachWaitDuration)
      } yield ()

      Await.result(future, Duration.Inf)

      val clusterBootstrap: ClusterBootstrap = childSession.build[ClusterBootstrap]
      clusterBootstrap.start()

      val selfUpRefFactory = childSession.build[ActorRef[MainActor.Command] => ActorRef[SelfUp]]
      selfUpRefFactory(ctx.self)

      Behaviors
        .receiveMessage[Command] { case MeUp =>
          Behaviors.same
        }
    }
  }

  private def startApplicationServer(
      session: Session,
      system: ActorSystem[_],
      host: String,
      port: Int,
      terminationHardDeadLine: FiniteDuration
  )(implicit classic: ClassicActorSystemProvider, ec: ExecutionContext): Future[Done] = {

    val threadController: ThreadController = session.build[ThreadController]

    val http = Http()
      .newServerAt(host, port)
      .bind(new Routes(threadController).toRoute)
      .map {
        _.addToCoordinatedShutdown(terminationHardDeadLine)
      }
    http.onComplete {
      case Success(serverBinding) =>
        val address = serverBinding.localAddress
        logger.info(s"server bound to http://${address.getHostString}:${address.getPort}")
      case Failure(ex) =>
        logger.error("Failed to bind endpoint, terminating system: $ex")
        system.terminate()
    }
    http.map(_ => Done)
  }

  private def startHealthCheckServer(
      system: ActorSystem[_],
      loadBalancerDetachWaitDuration: FiniteDuration
  )(implicit classic: ClassicActorSystemProvider, ec: ExecutionContext): Future[Done] = {
    val typeName            = "akka-management"
    val coordinatedShutdown = CoordinatedShutdown(system)
    val akkaManagement      = AkkaManagement(system)
    akkaManagement.start().map { _ =>
      logger
        .info(
          s"[$typeName] Server(${akkaManagement.settings.getHttpHostname}:${akkaManagement.settings.getHttpPort}) has started."
        )
      coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "management-terminate") { () =>
        for {
          _ <- akkaManagement.stop()
          _ <- Future {
            logger.info(s"[$typeName] $akkaManagement was terminated")
            logger.info(s"[$typeName] Wait for $loadBalancerDetachWaitDuration to detach from LoadBalancer")
          }
          _ <- akka.pattern.after(loadBalancerDetachWaitDuration) {
            Future {
              logger.info(s"[$typeName] Waiting for disconnection from LoadBalancer is completed.")
            }
          }
        } yield {
          Done
        }
      }
      Done
    }
  }
}
