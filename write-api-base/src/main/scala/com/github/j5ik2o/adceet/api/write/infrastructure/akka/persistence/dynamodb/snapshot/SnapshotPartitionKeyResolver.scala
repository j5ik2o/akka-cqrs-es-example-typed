package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb.snapshot

import com.github.j5ik2o.akka.persistence.dynamodb.model.{PersistenceId, SequenceNumber}
import com.github.j5ik2o.akka.persistence.dynamodb.snapshot.config.SnapshotPluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.snapshot.{PartitionKey, PartitionKeyResolver, ToPersistenceIdOps}
import com.github.j5ik2o.akka.persistence.dynamodb.utils.ConfigOps.ConfigOperations

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.DecimalFormat

class SnapshotPartitionKeyResolver(snapshotPluginConfig: SnapshotPluginConfig) extends PartitionKeyResolver with ToPersistenceIdOps {

  override def separator: String =
    snapshotPluginConfig.sourceConfig.valueAs[String]("persistence-id-separator", PersistenceId.Separator)

  // ${persistenceId.prefix}-${md5(persistenceId.reverse) % shardCount}
  override def resolve(persistenceId: PersistenceId, sequenceNumber: SequenceNumber): PartitionKey = {
    val md5          = MessageDigest.getInstance("MD5")
    val df           = new DecimalFormat("0000000000000000000000000000000000000000")
    val bytes        = persistenceId.asString.reverse.getBytes(StandardCharsets.UTF_8)
    val hash         = BigInt(md5.digest(bytes))
    val mod          = (hash.abs % snapshotPluginConfig.shardCount) + 1
    val modelNameOpt = persistenceId.prefix
    val pkey = modelNameOpt match {
      case Some(modelName) =>
        "%s-%s".format(modelName, df.format(mod))
      case None => // fallback
        df.format(mod)
    }
    PartitionKey(pkey)
  }

}
