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
import com.amazonaws.services.dynamodbv2.{ AmazonDynamoDB, AmazonDynamoDBClientBuilder }
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

object AmazonDynamoDBUtil {
  def createFromConfig(config: Config): AmazonDynamoDB = {
    val awsRegion: Regions =
      config.getAs[String]("region").map(s => Regions.fromName(s)).getOrElse(Regions.AP_NORTHEAST_1)
    val dynamoDBAccessKeyId: Option[String]     = config.getAs[String]("access-key-id")
    val dynamoDBSecretAccessKey: Option[String] = config.getAs[String]("secret-access-key")
    val dynamoDBEndpoint: Option[String]        = config.getAs[String]("endpoint")
    val clientConfigurationConfig               = config.getAs[Config]("client-configuration")

    create(
      awsRegion,
      dynamoDBAccessKeyId,
      dynamoDBSecretAccessKey,
      dynamoDBEndpoint,
      clientConfigurationConfig.map(ccc => ClientConfigurationUtil.createFromConfig(ccc))
    )
  }

  def create(
      awsRegion: Regions,
      dynamoDBAccessKeyId: Option[String],
      dynamoDBSecretAccessKey: Option[String],
      dynamoDBEndpoint: Option[String],
      clientConfiguration: Option[ClientConfiguration]
  ): AmazonDynamoDB = {
    val defaultBuilder = AmazonDynamoDBClientBuilder.standard
    (dynamoDBAccessKeyId, dynamoDBSecretAccessKey, dynamoDBEndpoint) match {
      case (Some(aki), Some(sak), Some(e)) =>
        val awsCredentialsProvider: AWSCredentialsProvider = new AWSStaticCredentialsProvider(
          new BasicAWSCredentials(aki, sak)
        )
        clientConfiguration
          .fold(defaultBuilder) { cc => defaultBuilder.withClientConfiguration(cc) }
          .withCredentials(awsCredentialsProvider)
          .withEndpointConfiguration(new EndpointConfiguration(e, awsRegion.getName))
          .build
      case _ =>
        clientConfiguration
          .fold(defaultBuilder) { cc => defaultBuilder.withClientConfiguration(cc) }
          .build()
    }
  }
}
