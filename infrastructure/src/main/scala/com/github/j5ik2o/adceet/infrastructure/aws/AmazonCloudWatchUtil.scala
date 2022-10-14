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
package com.github.j5ik2o.adceet.infrastructure.aws

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{ AWSCredentialsProvider, AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.{ AmazonCloudWatch, AmazonCloudWatchClientBuilder }
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

object AmazonCloudWatchUtil {
  def createFromConfig(config: Config): AmazonCloudWatch = {
    val awsRegion: Regions =
      config.getAs[String]("region").map(s => Regions.fromName(s)).getOrElse(Regions.AP_NORTHEAST_1)
    val accessKeyId: Option[String]     = config.getAs[String]("access-key-id")
    val secretAccessKey: Option[String] = config.getAs[String]("secret-access-key")
    val endpoint: Option[String]        = config.getAs[String]("endpoint")
    val clientConfigurationConfig       = config.getAs[Config]("client-configuration")
    create(
      awsRegion,
      accessKeyId,
      secretAccessKey,
      endpoint,
      clientConfigurationConfig.map(ccc => ClientConfigurationUtil.createFromConfig(ccc))
    )
  }

  def create(
      awsRegion: Regions,
      cloudWatchAccessKeyId: Option[String],
      cloudWatchSecretAccessKey: Option[String],
      cloudWatchEndpoint: Option[String],
      clientConfiguration: Option[ClientConfiguration]
  ): AmazonCloudWatch = {
    val builder = AmazonCloudWatchClientBuilder.standard
    (cloudWatchAccessKeyId, cloudWatchSecretAccessKey, cloudWatchEndpoint) match {
      case (Some(aki), Some(sak), Some(e)) =>
        val awsCredentialsProvider: AWSCredentialsProvider = new AWSStaticCredentialsProvider(
          new BasicAWSCredentials(aki, sak)
        )
        clientConfiguration
          .fold(builder) { cc => builder.withClientConfiguration(cc) }
          .withCredentials(awsCredentialsProvider)
          .withEndpointConfiguration(new EndpointConfiguration(e, awsRegion.getName))
          .build()
      case _ =>
        clientConfiguration
          .fold(builder) { cc => builder.withClientConfiguration(cc) }
          .build()
    }
  }

}
