package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb

import akka.actor.DynamicAccess
import com.github.j5ik2o.akka.persistence.dynamodb.client.v2.AwsCredentialsProviderProvider
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProvider, WebIdentityTokenFileCredentialsProvider }

import scala.annotation.unused

class V2AwsCredentialsProviderProviderImpl(@unused dynamicAccess: DynamicAccess, @unused pluginConfig: PluginConfig)
    extends AwsCredentialsProviderProvider {

  private val logger = LoggerFactory.getLogger(getClass)

  override def create: Option[AwsCredentialsProvider] = {
    if (sys.env.contains("AWS_ROLE_ARN")) {
      logger.info("AWS_ROLE_ARN = {}", sys.env("AWS_ROLE_ARN"))
      Some(WebIdentityTokenFileCredentialsProvider.create())
    } else
      None
  }
}
