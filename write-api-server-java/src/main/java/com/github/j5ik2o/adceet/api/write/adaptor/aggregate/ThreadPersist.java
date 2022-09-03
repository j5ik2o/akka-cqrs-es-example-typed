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
import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.RecoveryCompleted;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.SignalHandler;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.Empty;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.ThreadState;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.domain.events.ThreadEvent;
import wvlet.airframe.ulid.ULID;

public class ThreadPersist {
  record PersistCompleted(ThreadState state) {}

  record Persist(ThreadEvent threadEvent, ActorRef<PersistCompleted> replyTo) {}

  static class PersistentActor extends EventSourcedBehavior<Persist, ThreadEvent, ThreadState> {
    private final ActorContext<Persist> ctx;
    private final ThreadId id;
    private final ActorRef<ThreadAggregateProtocol.CommandRequest> parentRef;

    public PersistentActor(
        ActorContext<Persist> ctx,
        ThreadId id,
        ActorRef<ThreadAggregateProtocol.CommandRequest> parentRef) {
      super(PersistenceId.ofUniqueId(id.asString()));
      this.ctx = ctx;
      this.id = id;
      this.parentRef = parentRef;
    }

    @Override
    public ThreadState emptyState() {
      return new Empty(id);
    }

    @Override
    public CommandHandler<Persist, ThreadEvent, ThreadState> commandHandler() {
      var builder = newCommandHandlerBuilder();
      builder
          .forAnyState()
          .onCommand(
              Persist.class,
              (cmd) ->
                  Effect().persist(cmd.threadEvent).thenReply(cmd.replyTo, PersistCompleted::new));
      return builder.build();
    }

    @Override
    public EventHandler<ThreadState, ThreadEvent> eventHandler() {
      var builder = newEventHandlerBuilder();
      builder.forAnyState().onAnyEvent(ThreadState::applyEvent);
      return builder.build();
    }

    @Override
    public SignalHandler<ThreadState> signalHandler() {
      var builder = newSignalHandlerBuilder();
      builder.onSignal(
          RecoveryCompleted.class,
          (state, event) ->
              parentRef.tell(
                  new ThreadAggregateProtocol.StateRecoveryCompleted(ULID.newULID(), id, state)));
      return builder.build();
    }
  }
}
