package com.github.j5ik2o.api.write.infrastructure.metrics

import software.amazon.awssdk.core.metrics.CoreMetric

object AwsMetricNames {

  // @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/metrics-list.html
  object SdkMetricNames {
    val OperationName: MetricName = CoreMetric.OPERATION_NAME.name()

    val ApiCallSuccessful: String        = CoreMetric.API_CALL_SUCCESSFUL.name()
    val ApiCallDuration: String          = CoreMetric.API_CALL_DURATION.name()
    val CredentialsFetchDuration: String = CoreMetric.CREDENTIALS_FETCH_DURATION.name()
    val MarshallingDuration: String      = CoreMetric.MARSHALLING_DURATION.name()
    val RetryCount: String               = CoreMetric.RETRY_COUNT.name()
  }

  object CloudWatchLikeMetricNames {
    val Operation: TagKey = "Operation"

    object DynamoDb {
      val TableName: TagKey = "TableName"

      object Operations {
        val PutItem: TagValue        = "PutItem"
        val UpdateItem: TagValue     = "UpdateItem"
        val BatchWriteItem: TagValue = "BatchWriteItem"
        val Query: TagValue          = "Query"
      }
    }

    object S3 {
      val BucketName: TagKey = "BucketName"
    }
  }
}
