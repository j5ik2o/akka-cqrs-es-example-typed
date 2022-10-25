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
package com.github.j5ik2o.adceet.api.rmu

import akka.{ actor, Done }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior, PostStop, Terminated }
import com.amazonaws.auth.{ AWSCredentialsProvider, DefaultAWSCredentialsProviderChain }
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.{ asyncHealthCheck, healthCheck, healthy }
import com.github.j5ik2o.adceet.adaptor.healthchecks.k8s.{ bindAndHandleProbes, livenessProbe, readinessProbe }
import com.github.j5ik2o.adceet.infrastructure.aws.{
  AmazonCloudWatchUtil,
  AmazonDynamoDBStreamsUtil,
  AmazonDynamoDBUtil,
  CredentialsProviderUtil
}
import com.typesafe.config.{ Config, ConfigFactory }
import net.ceedubs.ficus.Ficus._
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import wvlet.airframe.ulid.ULID

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBStreams }
import org.slf4j.Logger

object Main extends App {
  sealed trait Command
  case object MeUp extends Command
  case class Abort(ex: Throwable) extends Command
  case class WrappedStarted(msg: ThreadReadModelUpdaterProtocol.Started) extends Command

  val id             = ULID.newULID
  val logger: Logger = LoggerFactory.getLogger(getClass)
  logger.debug("Starting the system...")

  val rootConfig: Config                      = ConfigFactory.load()
  val adceetConfig: Config                    = rootConfig.getConfig("adceet")
  val terminationHardDeadLine: FiniteDuration = adceetConfig.getDuration("termination-hard-dead-line").toMillis.millis
  val config: Config                          = adceetConfig.getConfig("read-model-updater.threads")
  val journalTableName: String                = config.getString("journal-table-name")

  val accessKeyIdOpt: Option[String] = config.getAs[String]("access-key-id")
  logger.debug("accessKeyIdOpt = {}", accessKeyIdOpt)
  val secretAccessKeyOpt: Option[String] = config.getAs[String]("secret-access-key")
  logger.debug("secretAccessKeyOpt = {}", secretAccessKeyOpt)

  val accountEventRouterClientConfig: Config = config.getAs[Config]("dynamodb-client").getOrElse(ConfigFactory.empty())
  val accountEventRouterStreamClientConfig: Config =
    config.getAs[Config]("dynamodb-stream-client").getOrElse(ConfigFactory.empty())
  val accountEventRouterCloudWatchConfig: Config =
    config.getAs[Config]("cloudwatch-client").getOrElse(ConfigFactory.empty())

  logger.debug("Creating an amazonDynamoDBClient...")
  val amazonDynamoDB: AmazonDynamoDB = AmazonDynamoDBUtil.createFromConfig(accountEventRouterClientConfig)
  logger.debug("Creating an amazonDynamoDBStreamsClient...")
  val amazonDynamoDBStreams: AmazonDynamoDBStreams =
    AmazonDynamoDBStreamsUtil.createFromConfig(accountEventRouterStreamClientConfig)
  logger.debug("Creating an amazonCloudWatchClient...")
  val amazonCloudwatch: AmazonCloudWatch = AmazonCloudWatchUtil.createFromConfig(accountEventRouterCloudWatchConfig)

  logger.debug("Creating an credentialsProvider...")
  val credentialsProvider: AWSCredentialsProvider = {
    (accessKeyIdOpt, secretAccessKeyOpt) match {
      case (Some(accessKeyId), Some(secretAccessKey)) =>
        CredentialsProviderUtil.createCredentialsProvider(Some(accessKeyId), Some(secretAccessKey))
      case _ =>
        DefaultAWSCredentialsProviderChain.getInstance()
    }
  }

  logger.debug("get the streamArn...")
  val streamArn: String = amazonDynamoDB.describeTable(journalTableName).getTable.getLatestStreamArn
  logger.debug("streamArn: {}", streamArn)

  def behavior: Behavior[Command] = Behaviors.setup[Command] { ctx =>
    implicit val system: actor.ActorSystem = ctx.system.classicSystem
    ctx.log.debug("start MainActor")

    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("slick", rootConfig)
    val childBehavior = ThreadReadModelUpdater(
      id,
      amazonDynamoDB,
      amazonDynamoDBStreams,
      amazonCloudwatch,
      credentialsProvider,
      None,
      None,
      databaseConfig.profile,
      databaseConfig.db,
      None,
      config
    )

    val rmuRef = ctx.spawn(childBehavior, "rmu")

    ctx.watch(rmuRef)

    val msgAdaptor = ctx.messageAdapter { msg => WrappedStarted(msg) }

    logger.debug("Starting the thread read model updater...")
    rmuRef ! ThreadReadModelUpdaterProtocol.StartWithReply(streamArn, msgAdaptor)

    Behaviors
      .receive[Command] {
        case (_, WrappedStarted(_)) =>
          logger.debug("Started the thread read model updater.")
          val serverBinding = httpServer()
          ctx.pipeToSelf(serverBinding) {
            case Success(Done) => MeUp
            case Failure(ex)   => Abort(ex)
          }
          Behaviors.same
        case (_, MeUp) =>
          logger.debug("Started the health checks.")
          Behaviors.same
        case (ctx, Abort(ex)) =>
          ctx.log.error("occurred error", ex)
          Behaviors.stopped
      }.receiveSignal {

        case (_, PostStop) =>
          logger.debug("Stopping the thread read model updater...")
          rmuRef ! ThreadReadModelUpdaterProtocol.Stop
          Behaviors.same
        case (_, Terminated(ref)) if ref == rmuRef =>
          logger.debug("Stopped the thread read model updater.")
          Behaviors.stopped
      }
  }

  private def httpServer()(implicit system: actor.ActorSystem): Future[Done] = {
    import system.dispatcher
    logger.debug("Starting the health checks...")
    val http = bindAndHandleProbes(
      readinessProbe(healthCheck("readiness_check")(healthy)),
      livenessProbe(asyncHealthCheck("liveness_check")(Future(healthy)))
    ).map {
      _.addToCoordinatedShutdown(terminationHardDeadLine)
    }
    http.onComplete {
      case Success(serverBinding) =>
        val address = serverBinding.localAddress
        logger.info(
          s"server bound to http://${address.getHostString}:${address.getPort}"
        )
      case Failure(ex) =>
        logger.error(s"Failed to bind endpoint, terminating system: $ex")
        system.terminate()
    }
    http.map(_ => Done)
  }

  val system: ActorSystem[Command] = ActorSystem(behavior, "adceet-read-model-updater", rootConfig)
  Await.result(system.whenTerminated, Duration.Inf)
}
