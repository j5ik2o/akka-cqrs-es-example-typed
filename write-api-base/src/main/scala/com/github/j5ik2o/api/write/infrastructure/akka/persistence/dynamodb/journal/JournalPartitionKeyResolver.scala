package com.github.j5ik2o.api.write.infrastructure.akka.persistence.dynamodb.journal

import com.github.j5ik2o.akka.persistence.dynamodb.config.JournalPluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.journal.{ PartitionKey, PartitionKeyResolver, ToPersistenceIdOps }
import com.github.j5ik2o.akka.persistence.dynamodb.model.{ PersistenceId, SequenceNumber }
import net.ceedubs.ficus.Ficus._

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.DecimalFormat

class JournalPartitionKeyResolver(pluginConfig: JournalPluginConfig)
    extends PartitionKeyResolver
    with ToPersistenceIdOps {

  private val digestAlgorithm      = "MD5"
  private val decimalFormatPattern = "0000000000000000000000000000000000000000"

  override def separator: String = pluginConfig.sourceConfig.getOrElse("separator", PersistenceId.Separator)

  override def resolve(persistenceId: PersistenceId, sequenceNumber: SequenceNumber): PartitionKey = {
    val bytes                        = persistenceId.asString.reverse.getBytes(StandardCharsets.UTF_8)
    val hash                         = BigInt(MessageDigest.getInstance(digestAlgorithm).digest(bytes))
    val mod                          = (hash.abs % pluginConfig.shardCount) + 1
    val modelNameOpt: Option[String] = persistenceId.prefix
    val df                           = new DecimalFormat(decimalFormatPattern)
    val pkey = modelNameOpt match {
      case Some(modelName) =>
        "%s-%s".format(modelName, df.format(mod))
      case None =>
        df.format(mod)
    }

    PartitionKey(pkey)
  }

}
