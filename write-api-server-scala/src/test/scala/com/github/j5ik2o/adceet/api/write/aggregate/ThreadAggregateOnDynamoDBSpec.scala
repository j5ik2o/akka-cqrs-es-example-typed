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
package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.Behavior
import com.github.dockerjava.core.DockerClientConfig
import com.github.j5ik2o.adceet.domain.ThreadId
import com.github.j5ik2o.adceet.infrastructure.serde.CborSerializable
import com.github.j5ik2o.adceet.test.util.RandomPortUtil
import com.github.j5ik2o.adceet.test.{ ActorSpec, LocalstackSpecSupport }
import com.github.j5ik2o.dockerController.{ DockerClientConfigUtil, DockerController }
import com.typesafe.config.{ Config, ConfigFactory }

object ThreadAggregateOnDynamoDBSpec {
  private val dockerClientConfig: DockerClientConfig = DockerClientConfigUtil.buildConfigAwareOfDockerMachine()
  val dockerHost: String                             = DockerClientConfigUtil.dockerHost(dockerClientConfig)
  val dynamoDbPort: Int                              = RandomPortUtil.temporaryServerPort()
  val config: Config = ConfigFactory.parseString(
    s"""
      |j5ik2o {
      |  dynamo-db-journal {
      |    class = "com.github.j5ik2o.akka.persistence.dynamodb.journal.DynamoDBJournal"
      |    plugin-dispatcher = "akka.actor.default-dispatcher"
      |    dynamo-db-client {
      |      access-key-id = "x"
      |      secret-access-key = "x"
      |      endpoint = "http://${ThreadAggregateOnDynamoDBSpec.dockerHost}:${ThreadAggregateOnDynamoDBSpec.dynamoDbPort}/"
      |    }
      |  }
      |  dynamo-db-snapshot {
      |    class = "com.github.j5ik2o.akka.persistence.dynamodb.snapshot.DynamoDBSnapshotStore"
      |    plugin-dispatcher = "akka.actor.default-dispatcher"
      |    dynamo-db-client {
      |      access-key-id = "x"
      |      secret-access-key = "x"
      |      endpoint = "http://${ThreadAggregateOnDynamoDBSpec.dockerHost}:${ThreadAggregateOnDynamoDBSpec.dynamoDbPort}/"
      |    }
      |  }
      |}
      |akka.loglevel = DEBUG
      |akka.actor.provider = local
      |akka.actor.serialization-bindings {
      |  "${classOf[CborSerializable].getName}" = jackson-cbor
      |}
      |akka.persistence.journal.plugin = "j5ik2o.dynamo-db-journal"
      |akka.persistence.snapshot-store.plugin = "j5ik2o.dynamo-db-snapshot"
      |""".stripMargin
  )
}

class ThreadAggregateOnDynamoDBSpec extends ActorSpec(ThreadAggregateOnDynamoDBSpec.config) with LocalstackSpecSupport {

  override lazy val hostName: String       = ThreadAggregateOnDynamoDBSpec.dockerHost
  override lazy val dynamodbLocalPort: Int = ThreadAggregateOnDynamoDBSpec.dynamoDbPort

  override protected val dockerControllers: Vector[DockerController] =
    Vector(dynamodbLocalController, cloudwatchController)

  override protected val waitPredicatesSettings: Map[DockerController, WaitPredicateSetting] =
    Map(
      dynamodbLocalController -> dynamodbWaitPredicateSetting,
      cloudwatchController    -> cloudwatchWaitPredicateSetting
    )

  override def afterStartContainers: Unit = {
    createJournalTable()
    createSnapshotTable()
  }

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

  "ThreadAggregate" - {
    "shouldCreateThread" in {
      underlying.shouldCreateThread()
    }
    "shouldAddMember" in {
      underlying.shouldAddMember()
    }
    "shouldAddMessage" in {
      underlying.shouldAddMessage()
    }
  }
}
