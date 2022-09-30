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

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.persistence.PersistentRepr
import akka.serialization.SerializationExtension
import akka.stream.scaladsl.Flow
import com.github.dockerjava.core.DockerClientConfig
import com.github.j5ik2o.adceet.api.write.aggregate.{
  AbstractThreadAggregateTestBase,
  ThreadAggregate,
  ThreadAggregateProtocol,
  ThreadPersist
}
import com.github.j5ik2o.adceet.domain.ThreadId
import com.github.j5ik2o.adceet.infrastructure.aws.{
  AmazonCloudWatchUtil,
  AmazonDynamoDBStreamsUtil,
  AmazonDynamoDBUtil,
  CredentialsProviderUtil
}
import com.github.j5ik2o.adceet.infrastructure.serde.CborSerializable
import com.github.j5ik2o.adceet.test.util.RandomPortUtil
import com.github.j5ik2o.adceet.test.{ ActorSpec, LocalstackSpecSupport }
import com.github.j5ik2o.ak.kcl.stage.CommittableRecord
import com.github.j5ik2o.dockerController.DockerClientConfigUtil
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import wvlet.airframe.ulid.ULID

import scala.util.{ Failure, Success }

object ThreadReadModelUpdaterSpec {
  val defaultAwsAccessKeyId = "x"
  val defaultAwsSecretKey   = "x"

  private val dockerClientConfig: DockerClientConfig = DockerClientConfigUtil.buildConfigAwareOfDockerMachine()

  val dockerHost: String = DockerClientConfigUtil.dockerHost(dockerClientConfig)

  val dynamodbPort: Int   = RandomPortUtil.temporaryServerPort()
  val cloudwatchPort: Int = RandomPortUtil.temporaryServerPort()
}

class ThreadReadModelUpdaterSpec
    extends ActorSpec(
      ConfigFactory
        .parseString(s"""
                                         |akka.persistence.journal.plugin = "j5ik2o.dynamo-db-journal"
                                         |akka.persistence.snapshot-store.plugin = "j5ik2o.dynamo-db-snapshot"
                                         |j5ik2o {
                                         |  dynamo-db-journal {
                                         |    class = "com.github.j5ik2o.akka.persistence.dynamodb.journal.DynamoDBJournal"
                                         |    plugin-dispatcher = "akka.actor.default-dispatcher"
                                         |    dynamo-db-client {
                                         |      access-key-id = "x"
                                         |      secret-access-key = "x"
                                         |      endpoint = "http://${ThreadReadModelUpdaterSpec.dockerHost}:${ThreadReadModelUpdaterSpec.dynamodbPort}/"
                                         |    }
                                         |  }
                                         |  dynamo-db-snapshot {
                                         |    class = "com.github.j5ik2o.akka.persistence.dynamodb.snapshot.DynamoDBSnapshotStore"
                                         |    plugin-dispatcher = "akka.actor.default-dispatcher"
                                         |    dynamo-db-client {
                                         |      access-key-id = "x"
                                         |      secret-access-key = "x"
                                         |      endpoint = "http://${ThreadReadModelUpdaterSpec.dockerHost}:${ThreadReadModelUpdaterSpec.dynamodbPort}/"
                                         |    }
                                         |  }
                                         |}
                                         |akka.loglevel = DEBUG
                                         |akka.actor.provider = local
                                         |akka.actor.serialization-bindings {
                                         |  "${classOf[CborSerializable].getName}" = jackson-cbor
                                         |}
                                         |akka.actor {
                                         |    allow-java-serialization = on
                                         |}
                                         |adceet.read-model-updater.threads {
                                         |  access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |  secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |  dynamodb-client {
                                         |    access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |    secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |    endpoint = "http://${ThreadReadModelUpdaterSpec.dockerHost}:${ThreadReadModelUpdaterSpec.dynamodbPort}/"
                                         |  }
                                         |  dynamodb-stream-client {
                                         |    access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |    secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |    endpoint = "http://${ThreadReadModelUpdaterSpec.dockerHost}:${ThreadReadModelUpdaterSpec.dynamodbPort}/"
                                         |  }
                                         |  cloudwatch-client {
                                         |    access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |    secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |    endpoint = "http://${ThreadReadModelUpdaterSpec.dockerHost}:${ThreadReadModelUpdaterSpec.cloudwatchPort}/"
                                         |  }
                                         |  application-name = "test"
                                         |  initial-position-in-stream = "TRIM_HORIZON"
                                         |}
                                         |""".stripMargin).withFallback(ConfigFactory.load())
    )
    with AnyFreeSpecLike
    with LocalstackSpecSupport
    with ScalaFutures {

  override lazy val hostName: String       = ThreadReadModelUpdaterSpec.dockerHost
  override lazy val dynamodbLocalPort: Int = ThreadReadModelUpdaterSpec.dynamodbPort
  override lazy val cloudwatchPort: Int    = ThreadReadModelUpdaterSpec.cloudwatchPort

  val underlying: AbstractThreadAggregateTestBase = new AbstractThreadAggregateTestBase(testKit) {
    override def behavior(id: ThreadId, inMemoryMode: Boolean): Behavior[ThreadAggregateProtocol.CommandRequest] = {
      ThreadAggregate.create(id) { (id, ref) =>
        ThreadPersist.persistBehavior(
          id,
          ref
        )
      }
    }
  }

  "ThreadReadModelUpdaterSpec" - {
    "shouldCreateThread" in {
      implicit val scheduler = system.scheduler
      val rootConfig         = system.settings.config

      val serialization = SerializationExtension(system)

      val id                                   = ULID.newULID
      val config                               = rootConfig.getConfig("adceet.read-model-updater.threads")
      val accessKeyId                          = config.getString("access-key-id")
      val secretAccessKey                      = config.getString("secret-access-key")
      val accountEventRouterClientConfig       = config.getConfig("dynamodb-client")
      val accountEventRouterStreamClientConfig = config.getConfig("dynamodb-stream-client")
      val accountEventRouterCloudWatchConfig   = config.getConfig("cloudwatch-client")
      val amazonDynamoDB                       = AmazonDynamoDBUtil.createFromConfig(accountEventRouterClientConfig)
      val amazonDynamoDBStreams = AmazonDynamoDBStreamsUtil.createFromConfig(accountEventRouterStreamClientConfig)
      val amazonCloudwatch      = AmazonCloudWatchUtil.createFromConfig(accountEventRouterCloudWatchConfig)

      val credentialsProvider =
        CredentialsProviderUtil.createCredentialsProvider(Some(accessKeyId), Some(secretAccessKey))
      val streamArn: String = amazonDynamoDB.describeTable(journalTableName).getTable.getLatestStreamArn

      var receivePid = ""

      val serializer = serialization.serializerFor(classOf[PersistentRepr])

      val readModelUpdaterRef = spawn(
        ThreadReadModelUpdater(
          id,
          amazonDynamoDB,
          amazonDynamoDBStreams,
          amazonCloudwatch,
          credentialsProvider,
          Flow[((String, Array[Byte]), CommittableRecord)].map { envelope =>
            val message = serialization
              .deserialize(envelope._1._2, classOf[PersistentRepr]).toEither.getOrElse(throw new Exception())
            val domainEvent = message.payload
            println(s"threadId = ${envelope._1._1}, domainEvent = $domainEvent")
            receivePid = envelope._1._1
            envelope._2
          },
          None,
          None,
          config
        )
      )

      readModelUpdaterRef ! ThreadReadModelUpdaterProtocol.Start(streamArn)

      Thread.sleep(10 * 1000)

      underlying.shouldCreateThread()

      Thread.sleep(10 * 1000)

      eventually {
        assert(receivePid.nonEmpty)
      }

      readModelUpdaterRef
        .ask[ThreadReadModelUpdaterProtocol.Stopped](ref =>
          ThreadReadModelUpdaterProtocol.StopWithReply(ref)
        ).futureValue
    }
//    "shouldAddMember" in {
//      underlying.shouldAddMember()
//    }
//    "shouldAddMessage" in {
//      underlying.shouldAddMessage()
//    }
  }

}
