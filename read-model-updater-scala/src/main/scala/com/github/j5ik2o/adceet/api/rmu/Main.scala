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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, PostStop}
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest
import com.github.j5ik2o.adceet.infrastructure.aws.{AmazonCloudWatchUtil, AmazonDynamoDBStreamsUtil, AmazonDynamoDBUtil, CredentialsProviderUtil}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import wvlet.airframe.ulid.ULID

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val id = ULID.newULID
  val rootConfig = ConfigFactory.load()
  val config = rootConfig.getConfig("adceet.read-model-updater.threads")
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

  val system = ActorSystem(behavior, "adceet-read-model-updater", rootConfig)
  Await.result(system.whenTerminated, Duration.Inf)
}
