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

import akka.{Done, actor}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, PostStop}
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.{asyncHealthCheck, healthCheck, healthy}
import com.github.j5ik2o.adceet.adaptor.healthchecks.k8s.{bindAndHandleProbes, livenessProbe, readinessProbe}
import com.github.j5ik2o.adceet.infrastructure.aws.{AmazonCloudWatchUtil, AmazonDynamoDBStreamsUtil, AmazonDynamoDBUtil, CredentialsProviderUtil}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import org.slf4j.LoggerFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import wvlet.airframe.ulid.ULID

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {
  val id = ULID.newULID
  val logger = LoggerFactory.getLogger(getClass)
  val rootConfig = ConfigFactory.load()
  val adceetConfig = rootConfig.getConfig("adceet")
  val terminationHardDeadLine = adceetConfig.getDuration("termination-hard-dead-line").toMillis.millis
  val config = adceetConfig.getConfig("read-model-updater.threads")
  val journalTableName = config.getString("journal-table-name")

  val accessKeyIdOpt = config.getAs[String]("access-key-id")
  val secretAccessKeyOpt = config.getAs[String]("secret-access-key")

  val accountEventRouterClientConfig = config.getAs[Config]("dynamodb-client").getOrElse(ConfigFactory.empty())
  val accountEventRouterStreamClientConfig = config.getAs[Config]("dynamodb-stream-client").getOrElse(ConfigFactory.empty())
  val accountEventRouterCloudWatchConfig = config.getAs[Config]("cloudwatch-client").getOrElse(ConfigFactory.empty())

  val amazonDynamoDB = AmazonDynamoDBUtil.createFromConfig(accountEventRouterClientConfig)
  val amazonDynamoDBStreams = AmazonDynamoDBStreamsUtil.createFromConfig(accountEventRouterStreamClientConfig)
  val amazonCloudwatch = AmazonCloudWatchUtil.createFromConfig(accountEventRouterCloudWatchConfig)

  val credentialsProvider: AWSCredentialsProvider = {
  (accessKeyIdOpt, secretAccessKeyOpt) match {
    case (Some(accessKeyId), Some(secretAccessKey)) =>
        CredentialsProviderUtil.createCredentialsProvider(Some(accessKeyId), Some(secretAccessKey))
    case _ =>
      DefaultAWSCredentialsProviderChain.getInstance()
  }
  }

  val streamArn: String = amazonDynamoDB.describeTable(journalTableName).getTable.getLatestStreamArn

  def behavior = Behaviors.setup[Any]{ ctx =>
    implicit val system: actor.ActorSystem = ctx.system.classicSystem
    ctx.log.debug("start MainActor")
    httpServer()

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

    rmuRef ! ThreadReadModelUpdaterProtocol.Start(streamArn)

    Behaviors.receive[Any]{
      case _ =>
        Behaviors.same
    }.receiveSignal{
      case (_, PostStop) =>
        rmuRef ! ThreadReadModelUpdaterProtocol.Stop
        Behaviors.same
    }
  }

  private def httpServer()(implicit system: actor.ActorSystem): Unit = {
    import system.dispatcher
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
    val result = http.map(_ => Done)
    Await.result(result, 10.seconds)
    logger.info(s" startHttpServer: finish")
  }

  val system = ActorSystem(behavior, "adceet-read-model-updater", rootConfig)
  Await.result(system.whenTerminated, Duration.Inf)
}
