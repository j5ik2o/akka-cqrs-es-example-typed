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
package com.github.j5ik2o.adceet.test

import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.model.{
  AttributeDefinition,
  CreateTableRequest,
  GlobalSecondaryIndex,
  KeySchemaElement,
  KeyType,
  Projection,
  ProjectionType,
  ProvisionedThroughput,
  ScalarAttributeType,
  StreamSpecification,
  StreamViewType
}
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBClientBuilder }
import com.github.j5ik2o.dockerController.WaitPredicates.WaitPredicate
import com.github.j5ik2o.dockerController.dynamodbLocal.DynamoDBLocalController
import com.github.j5ik2o.dockerController.{
  DockerContainerCreateRemoveLifecycle,
  DockerContainerStartStopLifecycle,
  DockerController,
  DockerControllerSpecSupport,
  WaitPredicates
}
import com.github.j5ik2o.dockerController.localstack.{ LocalStackController, Service }
import org.scalatest.TestSuite

import scala.concurrent.duration.{ Duration, DurationInt }
import scala.jdk.CollectionConverters._

trait LocalstackSpecSupport extends DockerControllerSpecSupport {
  this: TestSuite =>

  private val testTimeFactor: Int = sys.env.getOrElse("TEST_TIME_FACTOR", "1").toInt
  logger.debug(s"testTimeFactor = $testTimeFactor")

  override protected def createRemoveLifecycle: DockerContainerCreateRemoveLifecycle.Value =
    DockerContainerCreateRemoveLifecycle.ForAllTest

  override protected def startStopLifecycle: DockerContainerStartStopLifecycle.Value =
    DockerContainerStartStopLifecycle.ForAllTest

  val accessKeyId: String     = "AKIAIOSFODNN7EXAMPLE"
  val secretAccessKey: String = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"

  lazy val hostName: String       = dockerHost
  lazy val dynamodbLocalPort: Int = temporaryServerPort()
  lazy val cloudwatchPort: Int    = temporaryServerPort()

  val serviceEndpoint: String = s"http://$hostName:$dynamodbLocalPort"
  val region: Regions         = Regions.AP_NORTHEAST_1

  protected val dynamodbLocalController: DockerController = new DynamoDBLocalController(dockerClient, imageTag = None)(
    dynamodbLocalPort
  )

  protected val cloudwatchController: DockerController = LocalStackController(dockerClient)(
    services = Set(Service.DynamoDB, Service.DynamoDBStreams, Service.CloudWatch),
    edgeHostPort = cloudwatchPort,
    hostNameExternal = Some(dockerHost),
    defaultRegion = Some(region.getName)
  )

  override protected val dockerControllers: Vector[DockerController] =
    Vector(dynamodbLocalController, cloudwatchController)

  val waitPredicate: WaitPredicate = WaitPredicates.forLogMessageByRegex(
    DynamoDBLocalController.RegexOfWaitPredicate,
    Some((1 * testTimeFactor).seconds)
  )

  override protected val waitPredicatesSettings: Map[DockerController, WaitPredicateSetting] =
    Map(
      cloudwatchController    -> WaitPredicateSetting(Duration.Inf, WaitPredicates.forLogMessageExactly("Ready.")),
      dynamodbLocalController -> WaitPredicateSetting(Duration.Inf, waitPredicate)
    )

  protected val dynamoDBClient: AmazonDynamoDB = {
    AmazonDynamoDBClientBuilder
      .standard()
      .withEndpointConfiguration(new EndpointConfiguration(serviceEndpoint, region.getName))
      .withCredentials(
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey))
      )
      .build()
  }

  override def afterStartContainers: Unit = {
    createJournalTable()
    createSnapshotTable()
  }

  val journalTableName  = "Journal"
  val snapshotTableName = "Snapshot"

  protected def createJournalTable(): Unit = {
    val listTablesResult = dynamoDBClient.listTables(2)
    if (!listTablesResult.getTableNames.asScala.exists(_.contains(journalTableName))) {
      val createRequest = new CreateTableRequest()
        .withTableName(journalTableName)
        .withAttributeDefinitions(
          Seq(
            new AttributeDefinition().withAttributeName("pkey").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("skey").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("persistence-id").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("sequence-nr").withAttributeType(ScalarAttributeType.N),
            new AttributeDefinition().withAttributeName("tags").withAttributeType(ScalarAttributeType.S)
          ).asJava
        ).withKeySchema(
          Seq(
            new KeySchemaElement().withAttributeName("pkey").withKeyType(KeyType.HASH),
            new KeySchemaElement().withAttributeName("skey").withKeyType(KeyType.RANGE)
          ).asJava
        ).withProvisionedThroughput(
          new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L)
        ).withGlobalSecondaryIndexes(
          Seq(
            new GlobalSecondaryIndex()
              .withIndexName("TagsIndex").withKeySchema(
                Seq(
                  new KeySchemaElement().withAttributeName("tags").withKeyType(KeyType.HASH)
                ).asJava
              ).withProjection(new Projection().withProjectionType(ProjectionType.ALL))
              .withProvisionedThroughput(
                new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L)
              ),
            new GlobalSecondaryIndex()
              .withIndexName("GetJournalRowsIndex").withKeySchema(
                Seq(
                  new KeySchemaElement().withAttributeName("persistence-id").withKeyType(KeyType.HASH),
                  new KeySchemaElement().withAttributeName("sequence-nr").withKeyType(KeyType.RANGE)
                ).asJava
              ).withProjection(new Projection().withProjectionType(ProjectionType.ALL))
              .withProvisionedThroughput(
                new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L)
              )
          ).asJava
        ).withStreamSpecification(
          new StreamSpecification().withStreamEnabled(true).withStreamViewType(StreamViewType.NEW_IMAGE)
        )
      val createResponse = dynamoDBClient.createTable(createRequest)
      require(createResponse.getSdkHttpMetadata.getHttpStatusCode == 200)
    }
  }

  protected def createSnapshotTable(): Unit = {
    val listTablesResult = dynamoDBClient.listTables(2)
    if (!listTablesResult.getTableNames.asScala.exists(_.contains(snapshotTableName))) {
      val createRequest = new CreateTableRequest()
        .withTableName(snapshotTableName).withAttributeDefinitions(
          Seq(
            new AttributeDefinition().withAttributeName("pkey").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("skey").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("persistence-id").withAttributeType(ScalarAttributeType.S),
            new AttributeDefinition().withAttributeName("sequence-nr").withAttributeType(ScalarAttributeType.N)
          ).asJava
        ).withKeySchema(
          Seq(
            new KeySchemaElement().withAttributeName("pkey").withKeyType(KeyType.HASH),
            new KeySchemaElement().withAttributeName("skey").withKeyType(KeyType.RANGE)
          ).asJava
        )
        .withGlobalSecondaryIndexes(
          Seq(
            new GlobalSecondaryIndex()
              .withIndexName("GetSnapshotRowsIndex").withKeySchema(
                Seq(
                  new KeySchemaElement().withAttributeName("persistence-id").withKeyType(KeyType.HASH),
                  new KeySchemaElement().withAttributeName("sequence-nr").withKeyType(KeyType.RANGE)
                ).asJava
              ).withProjection(new Projection().withProjectionType(ProjectionType.ALL))
              .withProvisionedThroughput(
                new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L)
              )
          ).asJava
        )
        .withProvisionedThroughput(
          new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L)
        )
      val createResponse = dynamoDBClient.createTable(createRequest)
      require(createResponse.getSdkHttpMetadata.getHttpStatusCode == 200)
    }
  }

}
