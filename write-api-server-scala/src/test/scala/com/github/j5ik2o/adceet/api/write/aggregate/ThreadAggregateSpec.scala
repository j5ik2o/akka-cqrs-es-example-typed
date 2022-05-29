package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.Behavior
import com.github.j5ik2o.adceet.api.write.CborSerializable
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import com.typesafe.config.{ Config, ConfigFactory }

import java.util.UUID

object ThreadAggregateSpec {
  val config: Config = ConfigFactory.parseString(
    s"""
                akka.loglevel = DEBUG
                akka.actor.provider = local
                akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
                akka.persistence.journal.inmem.test-serialization = on
                akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
                akka.persistence.snapshot-store.local.dir = "target/snapshots/${classOf[
        ThreadAggregateSpec
      ].getName}-${UUID.randomUUID()}"
                akka.actor.serialization-bindings {
                    "${classOf[CborSerializable].getName}" = jackson-cbor
                }
                """
  )
}

class ThreadAggregateSpec extends ActorSpec(ThreadAggregateSpec.config) {

  val underlying: AbstractThreadAggregateTestBase = new AbstractThreadAggregateTestBase(testKit) {
    override def behavior(id: ThreadId, inMemoryMode: Boolean): Behavior[ThreadAggregateProtocol.CommandRequest] = {
      ThreadAggregate.create(id) { (id, ref) =>
        ThreadPersist.persistBehavior(
          id,
          ref
        )
      }
    }
  }

  "ThreadAggregate" - {
    "shouldCreateThread" in {
      underlying.shouldCreateThread()
    }
  }

}
