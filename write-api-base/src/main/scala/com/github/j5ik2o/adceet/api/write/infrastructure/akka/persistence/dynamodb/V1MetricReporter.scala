package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb

import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.metrics.{ MetricsReporter, Stopwatch }
import com.github.j5ik2o.akka.persistence.dynamodb.model.Context
import kamon.Kamon
import kamon.metric.MeasurementUnit

class V1MetricReporter(
    pluginConfig: PluginConfig
) extends MetricsReporter(pluginConfig) {
  import AkkaPersistenceMetricNames._
  import com.github.j5ik2o.adceet.api.write.infrastructure.metrics.AwsMetricNames._

  private val latencyHistogram =
    Kamon
      .histogram(PluginMetricNames.Latency, MeasurementUnit.time.milliseconds)
      .withTag(CloudWatchLikeMetricNames.DynamoDb.TableName, pluginConfig.tableName)
  private val errorCounter =
    Kamon
      .counter(PluginMetricNames.ErrorCount)
      .withTag(CloudWatchLikeMetricNames.DynamoDb.TableName, pluginConfig.tableName)

  private val asyncWriteMessagesLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "async-write-message")
  private val asyncWriteMessagesErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "async-write-message")

  private val asyncReplayMessagesLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "async-replay-message")
  private val asyncReplayMessagesErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "async-reply-message")

  private val readHighestSequenceNrLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "read-highest-sequence-nr")
  private val readHighestSequenceNrErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "read-highest-sequence-nr")

  private val serializeLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "serialize")
  private val serializeErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "serialize")

  private val deserializeLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "deserialize")
  private val deserializeErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "deserialize")

  override def beforeJournalAsyncWriteMessages(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }
  override def afterJournalAsyncWriteMessages(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    asyncWriteMessagesLatencyHistogram.record(elapsed.toMillis)
  }
  override def errorJournalAsyncWriteMessages(context: Context, ex: Throwable): Unit = {
    asyncWriteMessagesErrorCounter.increment()
  }

  override def beforeJournalAsyncReplayMessages(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }
  override def afterJournalAsyncReplayMessages(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    asyncReplayMessagesLatencyHistogram.record(elapsed.toMillis)
  }
  override def errorJournalAsyncReplayMessages(context: Context, ex: Throwable): Unit = {
    asyncReplayMessagesErrorCounter.increment()
  }

  override def beforeJournalAsyncReadHighestSequenceNr(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }
  override def afterJournalAsyncReadHighestSequenceNr(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    readHighestSequenceNrLatencyHistogram.record(elapsed.toMillis)
  }
  override def errorJournalAsyncReadHighestSequenceNr(context: Context, ex: Throwable): Unit = {
    readHighestSequenceNrErrorCounter.increment()
  }

  override def beforeJournalSerializeJournal(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }
  override def afterJournalSerializeJournal(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    serializeLatencyHistogram.record(elapsed.toMillis)
  }
  override def errorJournalSerializeJournal(context: Context, ex: Throwable): Unit = {
    serializeErrorCounter.increment()
  }

  override def beforeJournalDeserializeJournal(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }
  override def afterJournalDeserializeJournal(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    deserializeLatencyHistogram.record(elapsed.toMillis)
  }
  override def errorJournalDeserializeJournal(context: Context, ex: Throwable): Unit = {
    deserializeErrorCounter.increment()
  }

}
