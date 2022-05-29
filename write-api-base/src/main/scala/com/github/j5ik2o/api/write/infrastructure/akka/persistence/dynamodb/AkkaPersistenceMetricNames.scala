package com.github.j5ik2o.api.write.infrastructure.akka.persistence.dynamodb

import com.github.j5ik2o.api.write.infrastructure.metrics.{ MetricName, MetricNames }

object AkkaPersistenceMetricNames {
  private final val MetricNamePrefix: MetricName = s"${MetricNames.MetricNamePrefix}.journal.dynamodb"

  object PluginMetricNames {
    final val Latency: MetricName    = s"$MetricNamePrefix.plugin.latency"
    final val ErrorCount: MetricName = s"$MetricNamePrefix.plugin.error-count"
  }

  object AwsSdkMetricNames {
    final val ApiCallDuration: MetricName          = s"$MetricNamePrefix.aws.api-call-duration"
    final val CredentialsFetchDuration: MetricName = s"$MetricNamePrefix.aws.credentials-fetch-duration"
    final val MarshallingDuration: MetricName      = s"$MetricNamePrefix.aws.marshalling-duration"
    final val RetryCount: MetricName               = s"$MetricNamePrefix.aws.retry-count"
    final val ErrorCount: MetricName               = s"$MetricNamePrefix.aws.error-count"
  }
}
