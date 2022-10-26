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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;

import java.time.Duration;
import java.util.Optional;

public final class ShardedThreadAggregate {
    public static final EntityTypeKey<ThreadAggregateProtocol.CommandRequest> TypeKey =
            EntityTypeKey.create(ThreadAggregateProtocol.CommandRequest.class, "Thread");

    private static akka.japi.function.Function<EntityContext<ThreadAggregateProtocol.CommandRequest>, Behavior<ThreadAggregateProtocol.CommandRequest>>
    entityBehavior(Behavior<ThreadAggregateProtocol.CommandRequest> childBehavior) {
        return entityBehavior(childBehavior, null);
    }

    private static akka.japi.function.Function<EntityContext<ThreadAggregateProtocol.CommandRequest>, Behavior<ThreadAggregateProtocol.CommandRequest>>
    entityBehavior(Behavior<ThreadAggregateProtocol.CommandRequest> childBehavior, Duration receiveTimeout) {
        return entityContext -> Behaviors.setup(ctx -> {
            var childRef = ctx.spawn(childBehavior, ThreadAggregates.NAME);

            if (receiveTimeout != null) {
                ctx.setReceiveTimeout(receiveTimeout, new ThreadAggregateProtocol.Idle());
            }

            return Behaviors.<ThreadAggregateProtocol.CommandRequest>receiveMessage(cmd -> switch (cmd) {
                case ThreadAggregateProtocol.Idle ignore -> {
                    ctx.getLog().debug("entityBehavior#receive ThreadAggregateProtocol.Idle");
                    entityContext.getShard().tell(new ClusterSharding.Passivate<>(ctx.getSelf()));
                    yield Behaviors.same();
                }
                case ThreadAggregateProtocol.Stop ignore -> {
                    ctx.getLog().debug("entityBehavior#receive ThreadAggregateProtocol.Stop");
                    yield Behaviors.stopped();
                }
                default -> {
                    childRef.tell(cmd);
                    yield Behaviors.same();
                }
            });
        });
    }

    public static ActorRef<ShardingEnvelope<ThreadAggregateProtocol.CommandRequest>> initClusterSharding(
            ClusterSharding clusterSharding,
            Optional<Behavior<ThreadAggregateProtocol.CommandRequest>> childBehavior,
            Optional<Duration> receiveTimeout

    ) {
        var entity = Entity.of(
                TypeKey, entityBehavior(
                        childBehavior.orElse(Behaviors.empty()),
                        receiveTimeout.orElse(null)
                )
        ).withStopMessage(new ThreadAggregateProtocol.Stop());
        return clusterSharding.init(entity);
    }

    public static Behavior<ThreadAggregateProtocol.CommandRequest> ofProxy(ClusterSharding clusterSharding) {
        return Behaviors.receiveMessage(msg -> {
                    var entityRef = clusterSharding.entityRefFor(TypeKey, msg.threadId().asString());
                    entityRef.tell(msg);
                    return Behaviors.same();
                }
        );
    }

}