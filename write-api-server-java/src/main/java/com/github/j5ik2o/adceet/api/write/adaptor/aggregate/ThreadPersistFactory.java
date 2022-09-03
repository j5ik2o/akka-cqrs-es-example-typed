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
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.Empty;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.ThreadState;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import wvlet.airframe.ulid.ULID;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadPersistFactory {
    private static final ConcurrentHashMap<ThreadId, Optional<ThreadState>> PERSIST_STATES = new ConcurrentHashMap<ThreadId, Optional<ThreadState>>();

    public static void dispose() {
        PERSIST_STATES.clear();
    }

    public static Behavior<ThreadPersist.Persist> persistBehavior(
            ThreadId id, ActorRef<ThreadAggregateProtocol.CommandRequest> parentRef) {
        return Behaviors.setup(ctx -> new ThreadPersist.PersistentActor(ctx, id, parentRef));
    }

    private static Behavior<ThreadPersist.Persist> state(ThreadId id, ThreadState s) {
        return Behaviors.receiveMessage(msg -> switch (msg) {
                case ThreadPersist.Persist ignored -> {
                    var newState = s.applyEvent(msg.threadEvent());
                    msg.replyTo().tell(new ThreadPersist.PersistCompleted(newState));
                    PERSIST_STATES.put(id, Optional.of(newState));
                    yield state(id, newState);
                }
            });
    }

    public static Behavior<ThreadPersist.Persist> persistInMemoryBehavior(
            ThreadId id, ActorRef<ThreadAggregateProtocol.CommandRequest> parentRef
    ) {
        return Behaviors.setup(ctx -> {
            ThreadState initialState;
            var ps = PERSIST_STATES.getOrDefault(id, Optional.empty());
            if (ps.isPresent()) {
                parentRef.tell(new ThreadAggregateProtocol.StateRecoveryCompleted(ULID.newULID(), id, ps.get()));
                initialState = ps.get();
            } else {
                ThreadState state = new Empty(id);
                parentRef.tell(new ThreadAggregateProtocol.StateRecoveryCompleted(ULID.newULID(), id, ps.get()));
                initialState = state;
            }
            return state(id, initialState);
        });
    }

}
