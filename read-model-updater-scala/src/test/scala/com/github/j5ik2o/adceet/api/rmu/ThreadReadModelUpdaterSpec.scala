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

import com.github.j5ik2o.adceet.test.{ActorSpec, DynamoDBSpecSupport}
import com.github.j5ik2o.adceet.test.util.RandomPortUtil
import com.typesafe.config.ConfigFactory
import org.scalatest.freespec.AnyFreeSpecLike

object ThreadReadModelUpdaterSpec {
  val defaultAwsAccessKeyId = "x"
  val defaultAwsSecretKey = "x"

  val dynamoDbPort: Int = RandomPortUtil.temporaryServerPort()
  val cloudwatchPort: Int = RandomPortUtil.temporaryServerPort()
}

class ThreadReadModelUpdaterSpec extends ActorSpec(ConfigFactory.parseString(s"""
                                         |akka.persistence.journal.plugin = "j5ik2o.dynamo-db-journal"
                                         |akka.persistence.snapshot-store.plugin = "j5ik2o.dynamo-db-snapshot"
                                         |j5ik2o {
                                         |  dynamo-db-journal {
                                         |    class = "com.github.j5ik2o.akka.persistence.dynamodb.journal.DynamoDBJournal"
                                         |    plugin-dispatcher = "akka.actor.default-dispatcher"
                                         |    dynamo-db-client {
                                         |      access-key-id = "x"
                                         |      secret-access-key = "x"
                                         |      endpoint = "http://127.0.0.1:${ThreadReadModelUpdaterSpec.dynamoDbPort}/"
                                         |    }
                                         |  }
                                         |  dynamo-db-snapshot {
                                         |    class = "com.github.j5ik2o.akka.persistence.dynamodb.snapshot.DynamoDBSnapshotStore"
                                         |    plugin-dispatcher = "akka.actor.default-dispatcher"
                                         |    dynamo-db-client {
                                         |      access-key-id = "x"
                                         |      secret-access-key = "x"
                                         |      endpoint = "http://127.0.0.1:${ThreadReadModelUpdaterSpec.dynamoDbPort}/"
                                         |    }
                                         |  }
                                         |}
                                         |adceet.read-model-updater.threads {
                                         |  access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |  secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |  dynamodb-client {
                                         |    access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |    secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |    endpoint = "http://127.0.0.1:${ThreadReadModelUpdaterSpec.dynamoDbPort}/"
                                         |  }
                                         |  dynamodb-stream-client {
                                         |    access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |    secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |    endpoint = "http://127.0.0.1:${ThreadReadModelUpdaterSpec.dynamoDbPort}/"
                                         |  }
                                         |  cloudwatch-client {
                                         |    access-key-id = "${ThreadReadModelUpdaterSpec.defaultAwsAccessKeyId}"
                                         |    secret-access-key = "${ThreadReadModelUpdaterSpec.defaultAwsSecretKey}"
                                         |    endpoint = "http://127.0.0.1:${ThreadReadModelUpdaterSpec.cloudwatchPort}/"
                                         |  }
                                         |  application-name = "test"
                                         |  initial-position-in-stream = "TRIM_HORIZON"
                                         |}
                                         |""".stripMargin).withFallback(ConfigFactory.load()))
with AnyFreeSpecLike with DynamoDBSpecSupport{


}
