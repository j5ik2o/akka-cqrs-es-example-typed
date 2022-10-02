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

import akka.{ Done, NotUsed }
import net.ceedubs.ficus.Ficus._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }
import akka.stream.{ KillSwitches, UniqueKillSwitch }
import akka.stream.scaladsl.{ Flow, Keep, Sink }
import akka.stream.stage.AsyncCallback
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.dynamodbv2.model.{ BillingMode, DescribeStreamRequest }
import com.amazonaws.services.dynamodbv2.streamsadapter.model.RecordAdapter
import com.amazonaws.services.dynamodbv2.streamsadapter.{ AmazonDynamoDBStreamsAdapterClient, StreamsWorkerFactory }
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBStreams }
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{
  InitialPositionInStream,
  KinesisClientLibConfiguration,
  NoOpShardPrioritization,
  ShardSyncStrategyType,
  Worker
}
import com.amazonaws.services.kinesis.clientlibrary.types.{ InitializationInput, ShutdownInput }
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.github.j5ik2o.adceet.api.read.adaptor.dao.{ MembersSupport, MessagesSupport, ThreadsSupport }
import com.github.j5ik2o.adceet.api.rmu.ThreadReadModelUpdaterProtocol.{
  Command,
  Start,
  StartWithReply,
  Started,
  Stop,
  StopWithReply,
  Stopped,
  WrappedStartedResult,
  WrappedStoppedResult
}
import com.github.j5ik2o.ak.kcl.dsl.KCLSource
import com.github.j5ik2o.ak.kcl.stage.{ CommittableRecord, KCLSourceStage }
import com.github.j5ik2o.ak.kcl.stage.KCLSourceStage.RecordSet
import com.typesafe.config.Config
import slick.jdbc.JdbcProfile
import wvlet.airframe.ulid.ULID

import scala.concurrent.duration.{ Duration, _ }
import java.time.Instant
import java.util.Date
import scala.concurrent.{ Future, Promise }
import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success, Try }

object ThreadReadModelUpdater {
  def apply(
      id: ULID,
      amazonDynamoDB: AmazonDynamoDB,
      amazonDynamoDBStreams: AmazonDynamoDBStreams,
      amazonCloudWatch: AmazonCloudWatch,
      awsCredentialsProvider: AWSCredentialsProvider,
      producerFlow: Flow[((String, Array[Byte]), CommittableRecord), CommittableRecord, NotUsed],
      timestampAtInitialPositionInStream: Option[Instant],
      regionName: Option[String],

      config: Config
  ): Behavior[Command] = Behaviors.setup[Command] { ctx =>
    new ThreadReadModelUpdater(
      id,
      amazonDynamoDB,
      amazonDynamoDBStreams,
      amazonCloudWatch,
      awsCredentialsProvider,
      producerFlow,
      timestampAtInitialPositionInStream,
      regionName,

      config
    )(ctx)

  }
}

final class ThreadReadModelUpdater(
    id: ULID,
    amazonDynamoDB: AmazonDynamoDB,
    amazonDynamoDBStreams: AmazonDynamoDBStreams,
    amazonCloudWatch: AmazonCloudWatch,
    awsCredentialsProvider: AWSCredentialsProvider,
    producerFlow: Flow[((String, Array[Byte]), CommittableRecord), CommittableRecord, NotUsed],
    timestampAtInitialPositionInStream: Option[Instant],
    regionName: Option[String],
    config: Config
)(ctx: ActorContext[Command])
    extends AbstractBehavior(ctx)
    {
  implicit val system = ctx.system.toClassic
  import ctx.executionContext

  private var sw: UniqueKillSwitch = _
  private var future: Future[Done] = _

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Stop =>
        sw.shutdown()
        Behaviors.same
      case StopWithReply(replyTo) =>
        sw.shutdown()
        ctx.pipeToSelf(future) {
          case Success(_)  => WrappedStoppedResult(replyTo, Stopped())
          case Failure(ex) => throw ex
        }
        Behaviors.same
      case WrappedStartedResult(replyTo, msg) =>
        replyTo ! msg
        Behaviors.same
      case WrappedStoppedResult(replyTo, msg) =>
        replyTo ! msg
        Behaviors.same
      case Start(streamArn) =>
        startWorker(streamArn)
        Behaviors.same
      case StartWithReply(streamArn, replyTo) =>
        startWorker(streamArn)
        val start = Promise[Unit]()
        ctx.pipeToSelf(start.future) {
          case Success(_)  => WrappedStartedResult(replyTo, Started())
          case Failure(ex) => throw ex
        }
        start.success(())
        Behaviors.same
    }
  }

  private val convertToPidWithMessageFlow
      : Flow[CommittableRecord, ((String, Array[Byte]), CommittableRecord), NotUsed] = Flow[CommittableRecord].map {
    committableRecord =>
      val dynamoDb = committableRecord.record.asInstanceOf[RecordAdapter].getInternalObject.getDynamodb
      val newImage = dynamoDb.getNewImage.asScala
      val pid      = newImage("persistence-id").getS
      val message  = newImage("message").getB.array()
      (pid -> message, committableRecord)
  }

  private def startWorker(streamArn: String): Unit = {
    val describeStreamRequest = new DescribeStreamRequest().withStreamArn(streamArn)
    val describeStreamResult  = amazonDynamoDBStreams.describeStream(describeStreamRequest)
    val shards                = describeStreamResult.getStreamDescription.getShards.asScala
    ctx.log.debug(s"shards.size = ${shards.size}, shards = $shards")

    val result = KCLSource
      .ofCustomWorkerWithoutCheckpoint(
        workerF = newWorker(streamArn)
      ).viaMat(KillSwitches.single)(Keep.right)
      .via(convertToPidWithMessageFlow)
      .via(producerFlow)
      .map(_.checkpoint())
      .toMat(Sink.ignore)(Keep.both)
      .run()
    sw = result._1
    future = result._2
  }

  private def newWorker(streamArn: String)(
      initializationInputCallback: AsyncCallback[InitializationInput],
      recordSetCallback: AsyncCallback[RecordSet],
      shutdownInputCallback: AsyncCallback[Try[ShutdownInput]]
  ): Worker = {
    StreamsWorkerFactory.createDynamoDbStreamsWorker(
      KCLSourceStage
        .newRecordProcessorFactory(initializationInputCallback, recordSetCallback, shutdownInputCallback),
      createWorkerConfig(
        id,
        awsCredentialsProvider,
        streamArn,
        timestampAtInitialPositionInStream,
        regionName,
        config
      ),
      new AmazonDynamoDBStreamsAdapterClient(amazonDynamoDBStreams),
      amazonDynamoDB,
      amazonCloudWatch
    )
  }

  private def createWorkerConfig(
      id: ULID,
      awsCredentialsProvider: AWSCredentialsProvider,
      streamArn: String,
      timestampAtInitialPositionInStream: Option[Instant],
      regionName: Option[String],
      config: Config
  ): KinesisClientLibConfiguration = {
    val applicationName = config.getString("application-name")
    val position = config.getOrElse[String](
      "initial-position-in-stream",
      KinesisClientLibConfiguration.DEFAULT_INITIAL_POSITION_IN_STREAM.toString
    )
    val maxRecords = config.getOrElse[Int]("max-records", KinesisClientLibConfiguration.DEFAULT_MAX_RECORDS)
    val idleTimeBetweenReads = config.getOrElse(
      "idle-time-between-reads",
      KinesisClientLibConfiguration.DEFAULT_IDLETIME_BETWEEN_READS_MILLIS.millis
    )
    val failoverTime =
      config.getOrElse("failover-time", KinesisClientLibConfiguration.DEFAULT_FAILOVER_TIME_MILLIS.millis)
    val shardSyncInterval = config
      .getOrElse[Duration](
        "shard-sync-interval",
        KinesisClientLibConfiguration.DEFAULT_SHARD_SYNC_INTERVAL_MILLIS.millis
      )
    val callProcessRecordsEvenForEmptyRecordList = config.getOrElse(
      "call-process-records-even-for-empty-record-list",
      KinesisClientLibConfiguration.DEFAULT_DONT_CALL_PROCESS_RECORDS_FOR_EMPTY_RECORD_LIST
    )
    val parentShardPollInterval = config.getOrElse(
      "parent-shard-poll-interval",
      KinesisClientLibConfiguration.DEFAULT_PARENT_SHARD_POLL_INTERVAL_MILLIS.millis
    )
    val cleanupLeasesUponShardCompletion = config.getOrElse(
      "cleanup-leases-upon-shard-completion",
      KinesisClientLibConfiguration.DEFAULT_CLEANUP_LEASES_UPON_SHARDS_COMPLETION
    )
    // val withIgnoreUnexpectedChildShards = config.getOrElse("withIgnoreUnexpectedChildShards",KinesisClientLibConfiguration. )
    val userAgent = config.getOrElse("user-agent", KinesisClientLibConfiguration.KINESIS_CLIENT_LIB_USER_AGENT)
    val taskBackoffTime = config.getOrElse(
      "task-backoff-time",
      KinesisClientLibConfiguration.DEFAULT_TASK_BACKOFF_TIME_MILLIS.millis
    )
    val metricsBufferTime = config.getOrElse(
      "metrics-buffer-time",
      KinesisClientLibConfiguration.DEFAULT_METRICS_BUFFER_TIME_MILLIS.millis
    )
    val metricsMaxQueueSize =
      config.getOrElse("metrics-max-queue-size", KinesisClientLibConfiguration.DEFAULT_METRICS_MAX_QUEUE_SIZE)
    val metricsLevel =
      config.getOrElse("metrics-level", KinesisClientLibConfiguration.DEFAULT_METRICS_LEVEL.getName)
    val billingMode =
      config.getOrElse("billing-mode", KinesisClientLibConfiguration.DEFAULT_DDB_BILLING_MODE.toString)
    val validateSequenceNumberBeforeCheckpointing = config.getOrElse(
      "validate-sequence-number-before-checkpointing",
      KinesisClientLibConfiguration.DEFAULT_VALIDATE_SEQUENCE_NUMBER_BEFORE_CHECKPOINTING
    )
    val skipShardSyncAtStartupIfLeasesExist = config.getOrElse(
      "skip-shard-sync-at-startup-if-leases-exist",
      KinesisClientLibConfiguration.DEFAULT_SKIP_SHARD_SYNC_AT_STARTUP_IF_LEASES_EXIST
    )
    val shardSyncStrategyType = config.getOrElse(
      "shard-sync-strategy-type",
      KinesisClientLibConfiguration.DEFAULT_SHARD_SYNC_STRATEGY_TYPE.toString
    )
    val maxLeasesForWorker =
      config.getOrElse("max-leases-for-worker", KinesisClientLibConfiguration.DEFAULT_MAX_LEASES_FOR_WORKER)
    val maxLeasesToStealAtOneTime = config.getOrElse(
      "max-leases-to-steal-at-one-time",
      KinesisClientLibConfiguration.DEFAULT_MAX_LEASES_TO_STEAL_AT_ONE_TIME
    )
    val initialLeaseTableReadCapacity = config.getOrElse(
      "initial-lease-table-read-capacity",
      KinesisClientLibConfiguration.DEFAULT_INITIAL_LEASE_TABLE_READ_CAPACITY
    )
    val initialLeaseTableWriteCapacity = config.getOrElse(
      "initial-lease-table-write-capacity",
      KinesisClientLibConfiguration.DEFAULT_INITIAL_LEASE_TABLE_WRITE_CAPACITY
    )
    val maxLeaseRenewalThreads =
      config.getOrElse("max-lease-renewal-threads", KinesisClientLibConfiguration.DEFAULT_MAX_LEASE_RENEWAL_THREADS)

    val maxPendingProcessRecordsInput = config.getAs[Int]("max-pending-process-records-input")
    val retryGetRecordsInSeconds      = config.getAs[Duration]("retry-get-records")
    val maxGetRecordsThreadPool       = config.getAs[Int]("max-get-records-thread-pool")
    val maxCacheByteSize              = config.getAs[Int]("max-cache-byte-size")
    val dataFetchingStrategy          = config.getAs[String]("data-fetching-strategy")
    val maxRecordsCount               = config.getAs[Int]("max-records-count")
    val timeout                       = config.getAs[Duration]("timeout")
    val shutdownGrace =
      config.getOrElse("shutdown-grace", KinesisClientLibConfiguration.DEFAULT_SHUTDOWN_GRACE_MILLIS.millis)
    val idleMillisBetweenCalls       = config.getAs[Long]("idle-millis-between-calls")
    val logWarningForTaskAfterMillis = config.getAs[Duration]("log-warning-for-task-after")
    val listShardsBackoffTimeInMillis =
      config.getOrElse(
        "list-shards-backoff-time",
        KinesisClientLibConfiguration.DEFAULT_LIST_SHARDS_BACKOFF_TIME_IN_MILLIS.millis
      )
    val maxListShardsRetryAttempts = config.getOrElse(
      "max-list-shards-retry-attempts",
      KinesisClientLibConfiguration.DEFAULT_MAX_LIST_SHARDS_RETRY_ATTEMPTS
    )

    val baseWorkerConfig = new KinesisClientLibConfiguration(
      applicationName,
      streamArn,
      awsCredentialsProvider,
      id.toString()
    ).withInitialPositionInStream(InitialPositionInStream.valueOf(position))
      .withFailoverTimeMillis(failoverTime.toMillis)
      .withShardSyncIntervalMillis(shardSyncInterval.toMillis)
      .withMaxRecords(maxRecords)
      .withIdleTimeBetweenReadsInMillis(idleTimeBetweenReads.toMillis)
      .withCallProcessRecordsEvenForEmptyRecordList(callProcessRecordsEvenForEmptyRecordList)
      .withParentShardPollIntervalMillis(parentShardPollInterval.toMillis)
      .withCleanupLeasesUponShardCompletion(cleanupLeasesUponShardCompletion)
      // withIgnoreUnexpectedChildShards
      // withCommonClientConfig
      // withKinesisClientConfig
      // withDynamoDBClientConfig
      // withCloudWatchClientConfig
      .withUserAgent(userAgent)
      .withTaskBackoffTimeMillis(taskBackoffTime.toMillis)
      .withMetricsBufferTimeMillis(metricsBufferTime.toMillis)
      .withMetricsMaxQueueSize(metricsMaxQueueSize)
      .withMetricsLevel(MetricsLevel.valueOf(metricsLevel))
      .withBillingMode(BillingMode.valueOf(billingMode))
      // withMetricsLevel
      // withMetricsEnabledDimensions
      .withValidateSequenceNumberBeforeCheckpointing(validateSequenceNumberBeforeCheckpointing)
      .withSkipShardSyncAtStartupIfLeasesExist(skipShardSyncAtStartupIfLeasesExist)
      .withShardSyncStrategyType(ShardSyncStrategyType.valueOf(shardSyncStrategyType))
      .withMaxLeasesForWorker(maxLeasesForWorker)
      .withMaxLeasesToStealAtOneTime(maxLeasesToStealAtOneTime)
      .withInitialLeaseTableReadCapacity(initialLeaseTableReadCapacity)
      .withInitialLeaseTableWriteCapacity(initialLeaseTableWriteCapacity)
      .withShardPrioritizationStrategy(new NoOpShardPrioritization())
      .withMaxLeaseRenewalThreads(maxLeaseRenewalThreads)
      .withShutdownGraceMillis(shutdownGrace.toMillis)
      .withListShardsBackoffTimeInMillis(listShardsBackoffTimeInMillis.toMillis)
      .withMaxListShardsRetryAttempts(maxListShardsRetryAttempts)

    val c1 = timestampAtInitialPositionInStream.fold(baseWorkerConfig) { instant =>
      baseWorkerConfig.withTimestampAtInitialPositionInStream(Date.from(instant))
    }
    val c2  = regionName.fold(c1) { v => c1.withRegionName(v) }
    val c3  = retryGetRecordsInSeconds.fold(c2) { v => c2.withRetryGetRecordsInSeconds(v.toSeconds.toInt) }
    val c4  = maxGetRecordsThreadPool.fold(c3) { v => c3.withMaxGetRecordsThreadPool(v) }
    val c5  = maxPendingProcessRecordsInput.fold(c4) { v => c4.withMaxPendingProcessRecordsInput(v) }
    val c6  = maxCacheByteSize.fold(c5) { v => c5.withMaxCacheByteSize(v) }
    val c7  = dataFetchingStrategy.fold(c6) { v => c6.withDataFetchingStrategy(v) }
    val c8  = maxRecordsCount.fold(c7) { v => c7.withMaxRecordsCount(v) }
    val c9  = timeout.fold(c8) { v => c8.withTimeoutInSeconds(v.toSeconds.toInt); c8 }
    val c10 = idleMillisBetweenCalls.fold(c9) { v => c9.withIdleMillisBetweenCalls(v) }
    logWarningForTaskAfterMillis.fold(c10) { v => c10.withLogWarningForTaskAfterMillis(v.toMillis) }
  }
}
