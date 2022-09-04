package com.github.j5ik2o.adceet.api.write.adaptor.aggregate; /*
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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;

import java.util.function.Function;

public final class ThreadAggregates {
  public static final String NAME = "threads";

  public static Behavior<ThreadAggregateProtocol.CommandRequest> create(
      Function<ThreadId, String> nameF,
      Function<ThreadId, Behavior<ThreadAggregateProtocol.CommandRequest>> childBehaviorF) {
    return Behaviors.setup(
        ctx ->
            Behaviors.receiveMessage(
                msg -> {
                  var childRef = ctx.getChild(nameF.apply(msg.threadId()));
                  ActorRef<ThreadAggregateProtocol.CommandRequest> targetRef;
                  if (childRef.isEmpty()) {
                    targetRef =
                        ctx.spawn(
                            childBehaviorF.apply(msg.threadId()), nameF.apply(msg.threadId()));
                  } else {
                    targetRef = childRef.get().unsafeUpcast();
                  }
                  targetRef.tell(msg);
                  return Behaviors.same();
                }));
  }
}
