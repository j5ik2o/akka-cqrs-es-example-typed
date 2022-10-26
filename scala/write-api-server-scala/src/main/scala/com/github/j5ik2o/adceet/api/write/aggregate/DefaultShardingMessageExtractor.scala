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
package com.github.j5ik2o.adceet.api.write.aggregate

import akka.cluster.sharding.typed.{ ShardingEnvelope, ShardingMessageExtractor }

trait AggregateIdValueExtractable {
  def aggregateIdValue: String
}

final class DefaultShardingMessageExtractor[CMD <: AggregateIdValueExtractable](shardNum: Int)
    extends ShardingMessageExtractor[ShardingEnvelope[CMD], CMD] {

  override def entityId(message: ShardingEnvelope[CMD]): String = message.message.aggregateIdValue

  override def shardId(entityId: String): String = (math.abs(entityId.reverse.hashCode) % shardNum).toString

  override def unwrapMessage(message: ShardingEnvelope[CMD]): CMD = message.message
}
