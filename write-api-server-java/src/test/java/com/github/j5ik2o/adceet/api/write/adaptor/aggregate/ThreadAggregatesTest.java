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
package com.github.j5ik2o.adceet.api.write.adaptor.aggregate;

import akka.actor.typed.Behavior;
import com.github.j5ik2o.adceet.api.write.CborSerializable;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThreadAggregatesTest {
    static Config CONFIG = ConfigFactory.parseString(
            """
                    akka.loglevel = DEBUG
                    akka.actor.provider = local
                    akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
                    akka.persistence.journal.inmem.test-serialization = on
                    akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
                    akka.persistence.snapshot-store.local.dir = "target/snapshots/${javaClass.name}-${UUID.randomUUID()}"
                    akka.actor.serialization-bindings {
                        "%s" = jackson-cbor
                    }
                    """.formatted(CborSerializable.class.getName())
    );

    AbstractThreadAggregateTestBase underlying = new AbstractThreadAggregateTestBase() {
        @Override
        Behavior<ThreadAggregateProtocol.CommandRequest> behavior(ThreadId id, boolean inMemoryMode) {
            return ThreadAggregates.create(ThreadId::asString, ignore1 -> ThreadAggregateFactory.create(id, (ignore2, ref) -> {
                if (inMemoryMode) {
                    return ThreadPersistFactory.persistInMemoryBehavior(id, ref);
                } else {
                    return ThreadPersistFactory.persistBehavior(id, ref);
                }
            }));
        }
    };

    @BeforeAll
    public void beforeTest() {
        underlying.setUp(CONFIG);
    }

    @AfterEach
    public void afterEach() {
        underlying.clear();
    }

    @AfterAll
    public void afterTest() {
        underlying.tearDown();
    }

    @Test
    public void スレッドを作成できる() {
        underlying.shouldCreateThread();
    }

    @Test
    public void メンバーを追加できる() {
        underlying.shouldAddMember();
    }

    @Test
    public void メッセージを追加できる() {
        underlying.shouldAddMessage();
    }
}