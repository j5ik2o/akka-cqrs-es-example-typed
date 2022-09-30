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

import akka.actor.typed.Behavior
import akka.cluster.MemberStatus
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import com.github.j5ik2o.adceet.domain.ThreadId
import com.github.j5ik2o.adceet.infrastructure.serde.CborSerializable
import com.github.j5ik2o.adceet.test.ActorSpec
import com.typesafe.config.{ Config, ConfigFactory }

import java.util.UUID

object ShardedThreadAggregatesSpec {

  val config: Config = ConfigFactory.parseString(
    s"""
               akka.loglevel = DEBUG
               akka.actor.provider = cluster
               akka.remote {
                    artery {
                        enabled = on
                        transport = tcp
                        canonical {
                            port = 0
                        }
                    }
               }
               akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
               akka.persistence.journal.inmem.test-serialization = on
               akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
               akka.persistence.snapshot-store.local.dir = "target/snapshots/${classOf[
        ShardedThreadAggregatesSpec
      ].getName}-${UUID.randomUUID()}"
               akka.actor.serialization-bindings {
                "${classOf[CborSerializable].getName}" = jackson-cbor
               }
               """
  )

}

class ShardedThreadAggregatesSpec extends ActorSpec(ShardedThreadAggregatesSpec.config) {
  val underlying: AbstractThreadAggregateTestBase = new AbstractThreadAggregateTestBase(testKit) {
    override def behavior(id: ThreadId, inMemoryMode: Boolean): Behavior[ThreadAggregateProtocol.CommandRequest] = {
      val system          = testKit.system
      val cluster         = Cluster(system)
      val clusterSharding = ClusterSharding(system)
      cluster.manager ! Join(cluster.selfMember.address)
      eventually {
        assert(cluster.selfMember.status == MemberStatus.Up)
      }

      ShardedThreadAggregate.initClusterSharding(
        clusterSharding,
        Some(ThreadAggregates.create { _.asString } { id =>
          ThreadAggregate.create(id) { (id, ref) =>
            ThreadPersist.persistBehavior(
              id,
              ref
            )
          }
        })
      )

      ShardedThreadAggregate.ofProxy(clusterSharding)
    }
  }

  "ThreadAggregate" - {
    "shouldCreateThread" in {
      underlying.shouldCreateThread()
    }
    "shouldAddMember" in {
      underlying.shouldAddMember()
    }
    "shouldAddMessage" in {
      underlying.shouldAddMessage()
    }
  }
}
