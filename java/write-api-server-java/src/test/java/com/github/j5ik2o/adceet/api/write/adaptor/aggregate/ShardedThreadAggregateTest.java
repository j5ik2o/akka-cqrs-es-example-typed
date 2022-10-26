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
import akka.cluster.MemberStatus;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import com.github.j5ik2o.adceet.api.write.CborSerializable;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShardedThreadAggregateTest {
    static Config CONFIG = ConfigFactory.parseString(
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
                    akka.persistence.snapshot-store.local.dir = "target/snapshots/%s-%s"
                    akka.actor.serialization-bindings {
                     "%s" = jackson-cbor
                    }
                    """.formatted(ShardedThreadAggregateTest.class.getName(), UUID.randomUUID(), CborSerializable.class.getName())
    );

    AbstractThreadAggregateTestBase underlying = new AbstractThreadAggregateTestBase() {
        ClusterSharding clusterSharding = null;

        @Override
        Behavior<ThreadAggregateProtocol.CommandRequest> behavior(ThreadId id, boolean inMemoryMode) {
            return ShardedThreadAggregate.ofProxy(clusterSharding);
        }

        @Override
        void setUp(Config config) {
            super.setUp(config);
            var system = testKit().system();
            var cluster = Cluster.get(system);
            clusterSharding = ClusterSharding.get(system);
            cluster.manager().tell(new Join(cluster.selfMember().address()));
            await().until(() ->
                    cluster.selfMember().status() == MemberStatus.up()
            );

            ShardedThreadAggregate.initClusterSharding(
                    clusterSharding,
                    Optional.of(
                            ThreadAggregates.create(ThreadId::asString, id -> ThreadAggregateFactory.create(id, (ignore2, ref) -> {
                                if (inMemoryMode) {
                                    return ThreadPersistFactory.persistInMemoryBehavior(id, ref);
                                } else {
                                    return ThreadPersistFactory.persistBehavior(id, ref);
                                }
                            }))),
                    Optional.empty()
            );
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