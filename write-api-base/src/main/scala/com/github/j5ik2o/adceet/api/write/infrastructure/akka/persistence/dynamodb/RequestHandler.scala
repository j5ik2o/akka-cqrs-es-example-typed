package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb

import akka.actor.DynamicAccess
import com.amazonaws.handlers.RequestHandler2
import com.amazonaws.services.dynamodbv2.model.{
  BatchWriteItemRequest,
  PutItemRequest,
  QueryRequest,
  UpdateItemRequest
}
import com.amazonaws.{ AmazonWebServiceRequest, Request, Response }
import com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb.AkkaPersistenceMetricNames.AwsSdkMetricNames
import com.github.j5ik2o.adceet.api.write.infrastructure.metrics.AwsMetricNames.CloudWatchLikeMetricNames
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.metrics.Stopwatch
import com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb.AkkaPersistenceMetricNames.AwsSdkMetricNames
import com.github.j5ik2o.adceet.api.write.infrastructure.metrics.AwsMetricNames.CloudWatchLikeMetricNames
import kamon.Kamon
import kamon.metric.MeasurementUnit
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import scala.annotation.unused
import scala.jdk.CollectionConverters._

class RequestHandler(
    @unused dynamicAccess: DynamicAccess,
    @unused pluginConfig: PluginConfig
) extends RequestHandler2 {
  private val logger = LoggerFactory.getLogger(getClass)

  private val apiCallDurationHistogram =
    Kamon
      .histogram(AwsSdkMetricNames.ApiCallDuration, MeasurementUnit.time.milliseconds)
      .withTag("sdk", "v1")
  private val errorCounter =
    Kamon
      .counter(AwsSdkMetricNames.ErrorCount)
      .withTag("sdk", "v1")

  private val putItemApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)
  private val putItemErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.PutItem)

  private val updateItemApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem)
  private val updateItemErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.UpdateItem)

  private val batchWriteItemApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)
  private val batchWriteItemErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.BatchWriteItem)

  private val queryApiCallDurationHistogram = apiCallDurationHistogram
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)
  private val queryErrorCounter = errorCounter
    .withTag(CloudWatchLikeMetricNames.Operation, CloudWatchLikeMetricNames.DynamoDb.Operations.Query)

  private val stopwatches = new ConcurrentHashMap[AmazonWebServiceRequest, Stopwatch]().asScala

  override def beforeExecution(request: AmazonWebServiceRequest): AmazonWebServiceRequest = {
    stopwatches.put(request, Stopwatch.start())
    super.beforeExecution(request)
  }

  override def afterResponse(request: Request[_], response: Response[_]): Unit = {
    stopwatches.get(request.getOriginalRequest) match {
      case Some(sw) =>
        try {
          val elasped = sw.elapsed()
          request.getOriginalRequest match {
            case _: PutItemRequest =>
              putItemApiCallDurationHistogram.record(elasped.toMillis)
            case _: UpdateItemRequest =>
              updateItemApiCallDurationHistogram.record(elasped.toMillis)
            case _: BatchWriteItemRequest =>
              batchWriteItemApiCallDurationHistogram.record(elasped.toMillis)
            case _: QueryRequest =>
              queryApiCallDurationHistogram.record(elasped.toMillis)
            case _ =>
              logger.debug(s"no publish: ${request.getOriginalRequest}")
          }
        } finally {
          stopwatches.remove(request.getOriginalRequest)
        }
      case None =>
        logger.debug(s"no sw: ${request.getOriginalRequest}")
    }
  }

  override def afterError(request: Request[_], response: Response[_], e: Exception): Unit = {
    request.getOriginalRequest match {
      case _: PutItemRequest =>
        putItemErrorCounter.increment()
      case _: UpdateItemRequest =>
        updateItemErrorCounter.increment()
      case _: BatchWriteItemRequest =>
        batchWriteItemErrorCounter.increment()
      case _: QueryRequest =>
        queryErrorCounter.increment()
      case _ =>
        logger.debug(s"no publish: $request")
    }
  }

}
