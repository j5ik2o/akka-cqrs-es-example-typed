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
import com.github.j5ik2o.adceet.domain.ThreadId
import com.github.j5ik2o.adceet.infrastructure.serde.CborSerializable
import com.github.j5ik2o.adceet.test.ActorSpec
import com.typesafe.config.{Config, ConfigFactory}

import java.util.UUID

object ThreadAggregatesSpec {
  val config: Config = ConfigFactory.parseString(
    s"""
            akka.loglevel = DEBUG
            akka.actor.provider = local
            akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
            akka.persistence.journal.inmem.test-serialization = on
            akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
            akka.persistence.snapshot-store.local.dir = "target/snapshots/${classOf[
        ThreadAggregatesSpec
      ].getName}-${UUID.randomUUID()}"
            akka.actor.serialization-bindings {
                "${classOf[CborSerializable].getName}" = jackson-cbor
            }
            """
  )
}
class ThreadAggregatesSpec extends ActorSpec(ThreadAggregatesSpec.config) {
  val underlying: AbstractThreadAggregateTestBase = new AbstractThreadAggregateTestBase(testKit) {
    override def behavior(id: ThreadId, inMemoryMode: Boolean): Behavior[ThreadAggregateProtocol.CommandRequest] = {
      ThreadAggregates.create { _.asString } { id =>
        ThreadAggregate.create(id) { (id, ref) =>
          ThreadPersist.persistBehavior(
            id,
            ref
          )
        }
      }
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
