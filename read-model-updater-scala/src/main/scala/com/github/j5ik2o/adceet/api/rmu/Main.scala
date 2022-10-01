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

import akka.actor.typed.{ActorSystem, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.serialization.SerializationExtension
import akka.stream.scaladsl.Flow
import com.github.j5ik2o.adceet.infrastructure.aws.{AmazonCloudWatchUtil, AmazonDynamoDBStreamsUtil, AmazonDynamoDBUtil, CredentialsProviderUtil}
import com.github.j5ik2o.ak.kcl.stage.CommittableRecord
import com.typesafe.config.ConfigFactory
import wvlet.airframe.ulid.ULID
import akka.persistence.PersistentRepr
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.github.j5ik2o.adceet.domain.ThreadEvents.{MemberAdd, MessageAdd, ThreadCreated}
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val id = ULID.newULID
  val rootConfig = ConfigFactory.load()
  val config = rootConfig.getConfig("adceet.read-model-updater.threads")
  val journalTableName = config.getString("journal-table-name")

  val accessKeyIdOpt = config.getAs[String]("access-key-id")
  val secretAccessKeyOpt = config.getAs[String]("secret-access-key")

  val accountEventRouterClientConfig = config.getConfig("dynamodb-client")
  val accountEventRouterStreamClientConfig = config.getConfig("dynamodb-stream-client")
  val accountEventRouterCloudWatchConfig = config.getConfig("cloudwatch-client")

  val amazonDynamoDB = AmazonDynamoDBUtil.createFromConfig(accountEventRouterClientConfig)
  val amazonDynamoDBStreams = AmazonDynamoDBStreamsUtil.createFromConfig(accountEventRouterStreamClientConfig)
  val amazonCloudwatch = AmazonCloudWatchUtil.createFromConfig(accountEventRouterCloudWatchConfig)

  val credentialsProvider: AWSCredentialsProvider =
  (accessKeyIdOpt, secretAccessKeyOpt) match {
    case (Some(accessKeyId), Some(secretAccessKey)) =>
        CredentialsProviderUtil.createCredentialsProvider(Some(accessKeyId), Some(secretAccessKey))
    case _ =>
      DefaultAWSCredentialsProviderChain.getInstance()
  }

  val streamArn: String = amazonDynamoDB.describeTable(journalTableName).getTable.getLatestStreamArn

  def behavior = Behaviors.setup[Any]{ ctx =>

    val serialization = SerializationExtension(ctx.system)

    val childBehavior = ThreadReadModelUpdater(
      id,
      amazonDynamoDB,
      amazonDynamoDBStreams,
      amazonCloudwatch,
      credentialsProvider,
      Flow[((String, Array[Byte]), CommittableRecord)].map { envelope =>
        val message = serialization
          .deserialize(envelope._1._2, classOf[PersistentRepr]).toEither.getOrElse(throw new Exception())
        val domainEvent = message.payload
        domainEvent match {
          case event: ThreadCreated =>
          case event: MemberAdd =>
          case event: MessageAdd =>
        }

        println(s"domainEvent = $domainEvent")
        envelope._2
      },
      None,
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
