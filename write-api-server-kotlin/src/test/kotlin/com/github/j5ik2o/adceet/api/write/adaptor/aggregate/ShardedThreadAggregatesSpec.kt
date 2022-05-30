package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.Behavior
import akka.cluster.MemberStatus
import akka.cluster.sharding.typed.javadsl.ClusterSharding
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import com.github.j5ik2o.adceet.api.write.CborSerializable
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShardedThreadAggregatesSpec {

  companion object {
    val CONFIG: Config = ConfigFactory.parseString(
      """
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
               akka.persistence.snapshot-store.local.dir = "target/snapshots/${javaClass.name}-${UUID.randomUUID()}"
               akka.actor.serialization-bindings {
                "${CborSerializable::class.java.name}" = jackson-cbor
               }
               """
    )

    val underlying = object : AbstractThreadAggregateTestBase() {
      var clusterSharding: ClusterSharding? = null

      override fun behavior(
        id: ThreadId,
        inMemoryMode: Boolean
      ): Behavior<ThreadAggregateProtocol.CommandRequest> {
        return ShardedThreadAggregate.ofProxy(clusterSharding!!)
      }

      override fun setUp(config: Config) {
        super.setUp(config)
        val system = testKit().system()

        // クラスターへのJOIN
        val cluster = Cluster.get(system)
        clusterSharding = ClusterSharding.get(system)
        cluster.manager().tell(Join(cluster.selfMember().address()))
        await().until {
          cluster.selfMember().status() == MemberStatus.up()
        }

        // クラスターシャーディングの開始
        ShardedThreadAggregate.initClusterSharding(
          clusterSharding!!,
          ThreadAggregates.create(
            { it.asString() },
            { id ->
              ThreadAggregate.create(id) { id, ref ->
                if (inMemoryMode) {
                  ThreadPersist.persistInMemoryBehavior(
                    id,
                    ref
                  )
                } else {
                  ThreadPersist.persistBehavior(
                    id,
                    ref
                  )
                }
              }
            }
          )
        )
      }
    }
  }

  @BeforeAll
  fun beforeTest() {
    underlying.setUp(CONFIG)
  }

  @AfterEach
  fun afterEach() {
    underlying.clear()
  }

  @AfterAll
  fun afterTest() {
    underlying.tearDown()
  }

  @Test
  fun スレッドを作成できる() {
    underlying.shouldCreateThread()
  }

  @Test
  fun メンバーを追加できる() {
    underlying.shouldAddMember()
  }

  @Test
  fun メッセージを追加できる() {
    underlying.shouldAddMessage()
  }
}
