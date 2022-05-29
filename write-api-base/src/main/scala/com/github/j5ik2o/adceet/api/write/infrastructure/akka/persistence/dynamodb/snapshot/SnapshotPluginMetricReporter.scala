package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb.snapshot

import com.github.j5ik2o.adceet.api.write.infrastructure.metrics.AwsMetricNames.CloudWatchLikeMetricNames
import com.github.j5ik2o.adceet.api.write.infrastructure.metrics.{ MetricName, MetricNames }
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.metrics.{ MetricsReporter, Stopwatch }
import com.github.j5ik2o.akka.persistence.dynamodb.model.Context
import kamon.Kamon
import kamon.metric.MeasurementUnit

class SnapshotPluginMetricReporter(
    pluginConfig: PluginConfig
) extends MetricsReporter(pluginConfig) {

  import SnapshotPluginMetricNames._

  private val latencyHistogram =
    Kamon
      .histogram(Latency, MeasurementUnit.time.milliseconds)
      .withTag(CloudWatchLikeMetricNames.DynamoDb.TableName, pluginConfig.tableName)
  private val errorCounter =
    Kamon
      .counter(ErrorCount)
      .withTag(CloudWatchLikeMetricNames.DynamoDb.TableName, pluginConfig.tableName)

  private val loadAsyncLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-load-async")
  private val loadAsyncErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-load-async")

  private val saveAsyncLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-save-async")
  private val saveAsyncErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-save-async")

  private val serializeLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-serialize-snapshot")
  private val serializeErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-serialize-snapshot")

  private val deserializeLatencyHistogram = latencyHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-deserialize-snapshot")
  private val deserializeErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, "snapshot-store-deserialize-snapshot")

  override def beforeSnapshotStoreLoadAsync(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }

  override def afterSnapshotStoreLoadAsync(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    loadAsyncLatencyHistogram.record(elapsed.toMillis)
  }

  override def errorSnapshotStoreLoadAsync(context: Context, ex: Throwable): Unit = {
    loadAsyncErrorCounter.increment()
  }

  override def beforeSnapshotStoreSaveAsync(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }

  override def afterSnapshotStoreSaveAsync(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    saveAsyncLatencyHistogram.record(elapsed.toMillis)
  }

  override def errorSnapshotStoreSaveAsync(context: Context, ex: Throwable): Unit = {
    saveAsyncErrorCounter.increment()
  }

  override def beforeSnapshotStoreSerializeSnapshot(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }

  override def afterSnapshotStoreSerializeSnapshot(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    serializeLatencyHistogram.record(elapsed.toMillis)
  }

  override def errorSnapshotStoreSerializeSnapshot(context: Context, ex: Throwable): Unit = {
    serializeErrorCounter.increment()
  }

  override def beforeSnapshotStoreDeserializeSnapshot(context: Context): Context = {
    val sw = Stopwatch.start()
    context.withData(Some(sw))
  }

  override def afterSnapshotStoreDeserializeSnapshot(context: Context): Unit = {
    val elapsed = context.data.get.asInstanceOf[Stopwatch].elapsed()
    deserializeLatencyHistogram.record(elapsed.toMillis)
  }

  override def errorSnapshotStoreDeserializeSnapshot(context: Context, ex: Throwable): Unit = {
    deserializeErrorCounter.increment()
  }
}

private object SnapshotPluginMetricNames {
  private final val MetricNamePrefix: MetricName = s"${MetricNames.MetricNamePrefix}.snapshot.dynamodb"

  final val Latency: MetricName    = s"$MetricNamePrefix.plugin.latency"
  final val ErrorCount: MetricName = s"$MetricNamePrefix.plugin.error-count"
}
