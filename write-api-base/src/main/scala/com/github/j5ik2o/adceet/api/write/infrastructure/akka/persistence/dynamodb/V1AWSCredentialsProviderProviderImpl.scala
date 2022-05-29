package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb

import akka.actor.DynamicAccess
import com.amazonaws.auth.{ AWSCredentialsProvider, WebIdentityTokenCredentialsProvider }
import com.github.j5ik2o.akka.persistence.dynamodb.client.v1.AWSCredentialsProviderProvider
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import org.slf4j.LoggerFactory

import scala.annotation.unused

class V1AWSCredentialsProviderProviderImpl(@unused dynamicAccess: DynamicAccess, @unused pluginConfig: PluginConfig)
    extends AWSCredentialsProviderProvider {

  private val logger = LoggerFactory.getLogger(getClass)

  override def create: Option[AWSCredentialsProvider] = {
    if (sys.env.contains("AWS_ROLE_ARN")) {
      logger.info("AWS_ROLE_ARN = {}", sys.env("AWS_ROLE_ARN"))
      Some(WebIdentityTokenCredentialsProvider.create())
    } else None
  }

}
