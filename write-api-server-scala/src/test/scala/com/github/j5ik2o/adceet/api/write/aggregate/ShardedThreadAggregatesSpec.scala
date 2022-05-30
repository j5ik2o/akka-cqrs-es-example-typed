package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.Behavior
import akka.cluster.MemberStatus
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import com.github.j5ik2o.adceet.api.write.CborSerializable
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
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
        ThreadAggregates.create { _.asString } { id =>
          ThreadAggregate.create(id) { (id, ref) =>
            ThreadPersist.persistBehavior(
              id,
              ref
            )
          }
        }
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
