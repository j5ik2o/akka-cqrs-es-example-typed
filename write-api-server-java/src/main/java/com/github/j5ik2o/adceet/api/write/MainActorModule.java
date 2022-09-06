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
package com.github.j5ik2o.adceet.api.write;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.*;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.use_case.AddMemberInteractor;
import com.github.j5ik2o.adceet.api.write.use_case.AddMemberUseCase;
import com.github.j5ik2o.adceet.api.write.use_case.CreateThreadInteractor;
import com.github.j5ik2o.adceet.api.write.use_case.CreateThreadUseCase;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.util.Optional;

public class MainActorModule extends AbstractModule {
  private final ActorContext<MainProtocol.Command> ctx;
  private final RoleNames roleNames;

  public MainActorModule(ActorContext<MainProtocol.Command> ctx, RoleNames roleNames) {
    this.ctx = ctx;
    this.roleNames = roleNames;
  }

  @Provides
  private ClusterBootstrap clusterBootstrap() {
    return ClusterBootstrap.get(ctx.getSystem());
  }

  @Provides
  private ClusterSharding clusterSharding() {
    return ClusterSharding.get(ctx.getSystem());
  }

  @Provides
  private ActorRef<ThreadAggregateProtocol.CommandRequest> threadAggregate(
      ClusterSharding clusterSharding) {
    Optional<Behavior<ThreadAggregateProtocol.CommandRequest>> b = Optional.empty();
    if (roleNames.contains(RoleName.BACKEND)) {
      b =
          Optional.of(
              ThreadAggregates.create(
                  ThreadId::asString,
                  id ->
                      ThreadAggregateFactory.create(
                          id, (ignore2, ref) -> ThreadPersistFactory.persistBehavior(id, ref))));
    }
    ShardedThreadAggregate.initClusterSharding(clusterSharding, b, Optional.empty());
    return ctx.spawn(ShardedThreadAggregate.ofProxy(clusterSharding), "sharded-thread");
  }

  @Provides
  private CreateThreadUseCase createThreadUseCase(
      ActorRef<ThreadAggregateProtocol.CommandRequest> threadAggregateRef) {
    return new CreateThreadInteractor(ctx.getSystem(), threadAggregateRef);
  }

  @Provides
  private AddMemberUseCase addMemberUseCase(
      ActorRef<ThreadAggregateProtocol.CommandRequest> threadAggregateRef) {
    return new AddMemberInteractor(ctx.getSystem(), threadAggregateRef);
  }
}
