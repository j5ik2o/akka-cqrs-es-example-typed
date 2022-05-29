package com.github.j5ik2o.api.write.infrastructure.akka.persistence.dynamodb

import akka.actor.DynamicAccess
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import com.github.j5ik2o.api.write.infrastructure.metrics.MetricName
import kamon.Kamon
import kamon.metric.{ Counter, Histogram, MeasurementUnit }
import org.slf4j.LoggerFactory
import software.amazon.awssdk.metrics.{ MetricCollection, MetricPublisher }

import scala.annotation.unused
import scala.compat.java8.StreamConverters._

class V2MetricPublisher(
    @unused dynamicAccess: DynamicAccess,
    pluginConfig: PluginConfig
) extends MetricPublisher {
  import AkkaPersistenceMetricNames.AwsSdkMetricNames._
  import com.github.j5ik2o.api.write.infrastructure.metrics.AwsMetricNames._

  private val logger = LoggerFactory.getLogger(getClass)

  private def histogram(metricName: MetricName): Histogram =
    Kamon
      .histogram(metricName, MeasurementUnit.time.milliseconds)
      .withTag(CloudWatchLikeMetricNames.DynamoDb.TableName, pluginConfig.tableName)
      .withTag("sdk", s"java-${pluginConfig.clientConfig.clientVersion}")

  private def counter(metricName: MetricName): Counter =
    Kamon
      .counter(metricName)
      .withTag(CloudWatchLikeMetricNames.DynamoDb.TableName, pluginConfig.tableName)
      .withTag("sdk", s"java-${pluginConfig.clientConfig.clientVersion}")

  private val apiCallDurationHistogram          = histogram(ApiCallDuration)
  private val credentialsFetchDurationHistogram = histogram(CredentialsFetchDuration)
  private val marshallingDurationHistogram      = histogram(MarshallingDuration)
  private val retryCounter                      = counter(RetryCount)
  private val errorCounter                      = counter(ErrorCount)

  private val putItemApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)
  private val putItemCredentialsFetchDurationHistogram = credentialsFetchDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)
  private val putItemMarshallingDurationHistogram = marshallingDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)
  private val putItemRetryCounter = retryCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)
  private val putItemErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)

  private val updateItemApiCallDurationHistogram =
    apiCallDurationHistogram.withTag(
      CloudWatchLikeMetricNames.Operation,
      CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem
    )
  private val updateItemCredentialsFetchDurationHistogram = credentialsFetchDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem)
  private val updateItemMarshallingDurationHistogram = marshallingDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem)
  private val updateItemRetryCounter = retryCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem)
  private val updateItemErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem)

  private val batchWriteItemApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)
  private val batchWriteItemCredentialsFetchDurationHistogram = credentialsFetchDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)

  private val batchWriteItemMarshallingDurationHistogram = marshallingDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)
  private val batchWriteItemRetryCounter = retryCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)
  private val batchWriteItemErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)

  private val queryApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)
  private val queryCredentialsFetchDurationHistogram = credentialsFetchDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)
  private val queryMarshallingDurationHistogram = marshallingDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)
  private val queryRetryCounter = retryCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)
  private val queryErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)

  override def publish(metricCollection: MetricCollection): Unit = {
    import com.github.j5ik2o.api.write.infrastructure.metrics.AwsMetricNames.SdkMetricNames._
    val metricsMap = metricCollection.stream().toScala[Vector].map { mr => (mr.metric().name(), mr) }.toMap
    metricsMap(OperationName).value() match {
      case CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem =>
        if (metricsMap(ApiCallSuccessful).value().asInstanceOf[java.lang.Boolean]) {
          val apiCallDuration          = metricsMap(ApiCallDuration).value().asInstanceOf[java.time.Duration]
          val credentialsFetchDuration = metricsMap(CredentialsFetchDuration).value().asInstanceOf[java.time.Duration]
          val marshallingDuration      = metricsMap(MarshallingDuration).value().asInstanceOf[java.time.Duration]
          val retryCount               = metricsMap(RetryCount).value().asInstanceOf[java.lang.Integer]
          putItemApiCallDurationHistogram.record(apiCallDuration.toMillis)
          putItemCredentialsFetchDurationHistogram.record(credentialsFetchDuration.toMillis)
          putItemMarshallingDurationHistogram.record(marshallingDuration.toMillis)
          putItemRetryCounter.increment(retryCount.toLong)
        } else {
          putItemErrorCounter.increment()
        }
      case CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem =>
        if (metricsMap(ApiCallSuccessful).value().asInstanceOf[java.lang.Boolean]) {
          val apiCallDuration          = metricsMap(ApiCallDuration).value().asInstanceOf[java.time.Duration]
          val credentialsFetchDuration = metricsMap(CredentialsFetchDuration).value().asInstanceOf[java.time.Duration]
          val marshallingDuration      = metricsMap(MarshallingDuration).value().asInstanceOf[java.time.Duration]
          val retryCount               = metricsMap(RetryCount).value().asInstanceOf[java.lang.Integer]
          updateItemApiCallDurationHistogram.record(apiCallDuration.toMillis)
          updateItemCredentialsFetchDurationHistogram.record(credentialsFetchDuration.toMillis)
          updateItemMarshallingDurationHistogram.record(marshallingDuration.toMillis)
          updateItemRetryCounter.increment(retryCount.toLong)
        } else {
          updateItemErrorCounter.increment()
        }
      case CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem =>
        if (metricsMap(ApiCallSuccessful).value().asInstanceOf[java.lang.Boolean]) {
          val apiCallDuration          = metricsMap(ApiCallDuration).value().asInstanceOf[java.time.Duration]
          val credentialsFetchDuration = metricsMap(CredentialsFetchDuration).value().asInstanceOf[java.time.Duration]
          val marshallingDuration      = metricsMap(MarshallingDuration).value().asInstanceOf[java.time.Duration]
          val retryCount               = metricsMap(RetryCount).value().asInstanceOf[java.lang.Integer]
          batchWriteItemApiCallDurationHistogram.record(apiCallDuration.toMillis)
          batchWriteItemCredentialsFetchDurationHistogram.record(credentialsFetchDuration.toMillis)
          batchWriteItemMarshallingDurationHistogram.record(marshallingDuration.toMillis)
          batchWriteItemRetryCounter.increment(retryCount.toLong)
        } else {
          batchWriteItemErrorCounter.increment()
        }
      case CloudWatchLikeMetricNames.DynamoDb.Operations.Query =>
        if (metricsMap(ApiCallSuccessful).value().asInstanceOf[java.lang.Boolean]) {
          val apiCallDuration          = metricsMap(ApiCallDuration).value().asInstanceOf[java.time.Duration]
          val credentialsFetchDuration = metricsMap(CredentialsFetchDuration).value().asInstanceOf[java.time.Duration]
          val marshallingDuration      = metricsMap(MarshallingDuration).value().asInstanceOf[java.time.Duration]
          val retryCount               = metricsMap(RetryCount).value().asInstanceOf[java.lang.Integer]
          queryApiCallDurationHistogram.record(apiCallDuration.toMillis)
          queryCredentialsFetchDurationHistogram.record(credentialsFetchDuration.toMillis)
          queryMarshallingDurationHistogram.record(marshallingDuration.toMillis)
          queryRetryCounter.increment(retryCount.toLong)
        } else {
          queryErrorCounter.increment()
        }
      case unknownOperationName =>
        logger.debug(s"Operation $unknownOperationName was unhandled. Metrics will not publish. $metricsMap")
    }
  }

  override def close(): Unit = {}
}
