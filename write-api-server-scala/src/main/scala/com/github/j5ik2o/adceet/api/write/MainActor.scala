package com.github.j5ik2o.adceet.api.write

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, PostStop }
import akka.actor.{ ClassicActorSystemProvider, CoordinatedShutdown }
import akka.cluster.typed.{ Cluster, SelfUp }
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.github.j5ik2o.adceet.api.write.http.Routes
import com.github.j5ik2o.adceet.api.write.http.controller.ThreadController
import kamon.Kamon
import wvlet.log.io.StopWatch
// import kamon.Kamon
import org.slf4j.{ Logger, LoggerFactory }
import wvlet.airframe.{ DISupport, Session }

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }

object MainActor {
  sealed trait Command

  case object MeUp extends Command

  val logger: Logger = LoggerFactory.getLogger(classOf[MainActor])
}

class MainActor(val session: Session, stopWatch: StopWatch) extends DISupport {

  import MainActor._

  def create(args: Args): Behavior[Command] = {
    Behaviors.setup { ctx =>
      if (args.environment == Environments.Production)
        Kamon.init()

      logger.info(s"[${stopWatch.reportElapsedTime}] create")
      val cluster    = Cluster(ctx.system)
      val selfMember = cluster.selfMember
      logger.info(s"[${stopWatch.reportElapsedTime}] Specified role(s) = ${selfMember.roles.mkString(", ")}")

      logger.info(s"selfMember.roles = ${selfMember.roles}")
      require(selfMember.roles.nonEmpty, "akka.cluster.roles are empty")

      val roleNames = (
        if (selfMember.hasRole(RoleNames.Frontend.toString.toLowerCase))
          Seq(RoleNames.Frontend)
        else Seq.empty
      ) ++ (
        if (selfMember.hasRole(RoleNames.Backend.toString.toLowerCase))
          Seq(RoleNames.Backend)
        else Seq.empty
      )

      logger.info(s"roleNames = $roleNames")

      session.withChildSession(DISettings.mainActor(ctx, roleNames)) { childSession =>
        val config       = ctx.system.settings.config
        val adceetConfig = config.getConfig("adceet")
        val host         = adceetConfig.getString("http.host")
        val port         = adceetConfig.getInt("http.port")
        val terminationHardDeadLine =
          adceetConfig.getDuration("management.http.termination-hard-deadline").toMillis.millis
        val loadBalancerDetachWaitDuration =
          adceetConfig.getDuration("management.http.load-balancer-detach-wait-duration").toMillis.millis

        implicit val system: ActorSystem[_]       = ctx.system
        implicit val ec: ExecutionContextExecutor = ctx.executionContext

        val future = for {
          _ <-
            if (roleNames.contains(RoleNames.Frontend))
              startHttpServer(childSession, ctx.system, host, port, terminationHardDeadLine)
            else
              Future.successful(())
          _ <- startHealthCheckServer(ctx.system, loadBalancerDetachWaitDuration)
        } yield ()

        Await.result(future, 10.seconds)

        val clusterBootstrap: ClusterBootstrap = childSession.build[ClusterBootstrap]
        clusterBootstrap.start()

        childSession.build[ActorRef[SelfUp]]

        Behaviors
          .receiveMessage[Command] { case MeUp =>
            Behaviors.same
          }.receiveSignal { case (_, PostStop) =>
            if (args.environment == Environments.Production)
              Kamon.stop()
            Behaviors.same
          }
      }
    }
  }

  private def startHttpServer(
      session: Session,
      system: ActorSystem[_],
      host: String,
      port: Int,
      terminationHardDeadLine: FiniteDuration
  )(implicit classic: ClassicActorSystemProvider, ec: ExecutionContext): Future[Done] = {
    logger.info(s"[${stopWatch.reportElapsedTime}] startHttpServer: start")

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

  private def startHealthCheckServer(
      system: ActorSystem[_],
      loadBalancerDetachWaitDuration: FiniteDuration
  )(implicit classic: ClassicActorSystemProvider, ec: ExecutionContext): Future[Done] = {
    logger.info(s"[${stopWatch.reportElapsedTime}] startHealthCheckServer: start")

    val typeName            = "akka-management"
    val coordinatedShutdown = CoordinatedShutdown(system)
    val akkaManagement      = AkkaManagement(system)
    val result = akkaManagement.start().map { _ =>
      logger
        .info(
          s"[${stopWatch.reportElapsedTime}] [$typeName] Server(${akkaManagement.settings.getHttpHostname}:${akkaManagement.settings.getHttpPort}) has started."
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
    logger.info(s"[${stopWatch.reportElapsedTime}] startHealthCheckServer: finish")
    result
  }
}
