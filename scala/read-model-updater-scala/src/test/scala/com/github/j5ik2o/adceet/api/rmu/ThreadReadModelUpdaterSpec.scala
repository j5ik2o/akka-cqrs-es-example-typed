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
import com.github.dockerjava.core.DockerClientConfig
import com.github.j5ik2o.adceet.api.read.adaptor.dao.ThreadsSupport
import com.github.j5ik2o.adceet.api.write.aggregate.{
  AbstractThreadAggregateTestBase,
  ThreadAggregate,
  ThreadAggregateProtocol,
  ThreadPersist
}
import com.github.j5ik2o.adceet.domain.ThreadEvents.ThreadEvent
import com.github.j5ik2o.adceet.domain.ThreadId
import com.github.j5ik2o.adceet.infrastructure.aws.{
  AmazonCloudWatchUtil,
  AmazonDynamoDBStreamsUtil,
  AmazonDynamoDBUtil,
  CredentialsProviderUtil
}
import com.github.j5ik2o.adceet.infrastructure.serde.CborSerializable
import com.github.j5ik2o.adceet.test.util.RandomPortUtil
import com.github.j5ik2o.adceet.test.{ ActorSpec, LocalstackSpecSupport, Slick3SpecSupport }
import com.github.j5ik2o.dockerController.flyway.{ FlywayConfig, FlywaySpecSupport }
import com.github.j5ik2o.dockerController.mysql.MySQLController
import com.github.j5ik2o.dockerController.{ DockerClientConfigUtil, DockerController, WaitPredicates }
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpecLike
import slick.jdbc.JdbcProfile
import wvlet.airframe.ulid.ULID

import scala.concurrent.Future
import scala.concurrent.duration.{ Duration, DurationDouble }

object ThreadReadModelUpdaterSpec {
  val defaultAwsAccessKeyId = "x"
  val defaultAwsSecretKey   = "x"

  private val dockerClientConfig: DockerClientConfig = DockerClientConfigUtil.buildConfigAwareOfDockerMachine()

  val dockerHost: String = DockerClientConfigUtil.dockerHost(dockerClientConfig)

  val dynamodbPort: Int   = RandomPortUtil.temporaryServerPort()
  val cloudwatchPort: Int = RandomPortUtil.temporaryServerPort()
  val mysqlPort: Int      = RandomPortUtil.temporaryServerPort()
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
    with Slick3SpecSupport
    with ThreadsSupport
    with ScalaFutures
    with FlywaySpecSupport {

  override lazy val hostName: String       = ThreadReadModelUpdaterSpec.dockerHost
  override lazy val dynamodbLocalPort: Int = ThreadReadModelUpdaterSpec.dynamodbPort
  override lazy val cloudwatchPort: Int    = ThreadReadModelUpdaterSpec.cloudwatchPort

  override lazy val profile: JdbcProfile = slickJdbcProfile

  override protected def tables: Seq[String] = Seq.empty

  override lazy val jdbcDriverClassName: String = classOf[com.mysql.cj.jdbc.Driver].getName
  override lazy val dbHost: String              = hostName
  override lazy val dbPort: Int                 = ThreadReadModelUpdaterSpec.mysqlPort
  override lazy val dbName: String              = "adceet"
  override lazy val dbUserName: String          = "root"
  override lazy val dbPassword: String          = "test"

  override protected def flywayDriverClassName: String = classOf[com.mysql.cj.jdbc.Driver].getName
  override protected def flywayDbHost: String          = hostName
  override protected def flywayDbHostPort: Int         = dbPort
  override protected def flywayDbName: String          = dbName
  override protected def flywayDbUserName: String      = dbUserName
  override protected def flywayDbPassword: String      = dbPassword
  override protected def flywayJDBCUrl: String =
    s"jdbc:mysql://$flywayDbHost:$flywayDbHostPort/$flywayDbName?allowPublicKeyRetrieval=true&useSSL=false&user=$flywayDbUserName&password=$flywayDbPassword"

  val mysqlController: MySQLController = MySQLController(dockerClient)(dbPort, dbPassword, databaseName = Some(dbName))

  val mysqlWaitPredicateSetting: WaitPredicateSetting = WaitPredicateSetting(
    Duration.Inf,
    WaitPredicates.forLogMessageByRegex(
      """.*MySQL init process done\. Ready for start up\.""".r,
      Some((1 * testTimeFactor).seconds)
    )
  )

  override protected val dockerControllers: Vector[DockerController] =
    Vector(mysqlController, dynamodbLocalController, cloudwatchController)

  override protected val waitPredicatesSettings: Map[DockerController, WaitPredicateSetting] =
    Map(
      dynamodbLocalController -> dynamodbWaitPredicateSetting,
      cloudwatchController    -> cloudwatchWaitPredicateSetting,
      mysqlController         -> mysqlWaitPredicateSetting
    )

  override def afterStartContainers: Unit = {
    createJournalTable()
    createSnapshotTable()
    val flywayContext = createFlywayContext(FlywayConfig(Seq("classpath:flyway")))
    flywayContext.flyway.migrate()
    setUpSlick()
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

  "ThreadReadModelUpdaterSpec" - {
    "shouldCreateThread" in {

      implicit val scheduler = system.scheduler
      val rootConfig         = system.settings.config

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

      val readModelUpdaterRef = spawn(
        ThreadReadModelUpdater(
          id,
          amazonDynamoDB,
          amazonDynamoDBStreams,
          amazonCloudwatch,
          credentialsProvider,
          None,
          None,
          slickJdbcProfile,
          slickDbConfig.db,
          Some { event: ThreadEvent =>
            receivePid = event.threadId.asString
            Future.successful(())
          },
          config
        )
      )

      readModelUpdaterRef ! ThreadReadModelUpdaterProtocol.Start(streamArn)

      java.lang.Thread.sleep(10 * 1000)

      underlying.shouldAddMessage()

      java.lang.Thread.sleep(10 * 1000)

      eventually {
        assert(receivePid.nonEmpty)
      }
//      val stopProbe = testKit.createTestProbe[ThreadReadModelUpdaterProtocol.Stopped]()
//      readModelUpdaterRef ! ThreadReadModelUpdaterProtocol.StopWithReply(stopProbe.ref)
//      stopProbe.expectMessage((10 * testTimeFactor).seconds, ThreadReadModelUpdaterProtocol.Stopped())
    }
  }
}
