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
import akka.actor.typed.javadsl.*;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.Empty;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.Just;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.ThreadState;
import com.github.j5ik2o.adceet.api.write.domain.ThreadFactory;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.domain.errors.ThreadError;
import com.github.j5ik2o.adceet.api.write.domain.events.ThreadCreated;
import com.github.j5ik2o.adceet.api.write.domain.events.*;
import io.vavr.control.Either;
import wvlet.airframe.ulid.ULID;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ThreadAggregate extends AbstractBehavior<ThreadAggregateProtocol.CommandRequest> {
    private final StashBuffer<ThreadAggregateProtocol.CommandRequest> stashBuffer;
    private final ThreadId id;
    private final BiFunction<ThreadId, ActorRef<ThreadAggregateProtocol.CommandRequest>, Behavior<ThreadPersist.Persist>> persistBehaviorF;

    private final ActorRef<ThreadPersist.Persist> persistRef;
    private final ActorContext<ThreadAggregateProtocol.CommandRequest> ctx;

    public ThreadAggregate(ActorContext<ThreadAggregateProtocol.CommandRequest> ctx,
                           StashBuffer<ThreadAggregateProtocol.CommandRequest> stashBuffer,
                           ThreadId id,
                           BiFunction<ThreadId, ActorRef<ThreadAggregateProtocol.CommandRequest>, Behavior<ThreadPersist.Persist>> persistBehaviorF) {
        super(ctx);
        this.ctx = ctx;
        this.stashBuffer = stashBuffer;
        this.id = id;
        this.persistBehaviorF = persistBehaviorF;
        persistRef = ctx.spawn(persistBehaviorF.apply(id, ctx.getSelf()), "persist");
    }

    private Behavior<ThreadAggregateProtocol.CommandRequest> persist(
            ThreadEvent threadEvent,
            Function<ThreadState, Behavior<ThreadAggregateProtocol.CommandRequest>> succ
    ) {
        var messageAdaptor = ctx.messageAdapter(ThreadPersist.PersistCompleted.class, msg ->
                new ThreadAggregateProtocol.WrappedPersistReply(ULID.newULID(), id, msg)
        );
        persistRef.tell(new ThreadPersist.Persist(threadEvent, messageAdaptor));
        return Behaviors.receiveMessage(msg ->
                switch (msg) {
                    case ThreadAggregateProtocol.WrappedPersistReply r -> succ.apply(r.message().state());
                    default -> Behaviors.unhandled();
                }
        );
    }

    private Receive<ThreadAggregateProtocol.CommandRequest> just(Just state) {
        var builder = newReceiveBuilder();
        builder.onMessage(ThreadAggregateProtocol.AddMember.class, msg -> {
                    var result = state.thread().addMember(msg.accountId());
                    return switch (result) {
                        case Either.Right<ThreadError, MemberAdd> r -> persist(r.get(), it -> {
                            msg.replyTo().tell(new ThreadAggregateProtocol.AddMemberSucceeded(ULID.newULID(), msg.id(), id));
                            return just((Just) it);
                        });
                        case Either.Left<ThreadError, MemberAdd> l -> {
                            msg.replyTo().tell(new ThreadAggregateProtocol.AddMemberFailed(ULID.newULID(), msg.id(), id, l.getLeft()));
                            yield just(state);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + result);
                    };
                }
        );
        builder.onMessage(ThreadAggregateProtocol.AddMessage.class, msg -> {
            var result = state.thread().addMessage(msg.message().senderId(), msg.message().id(), msg.message().body());
            return switch (result) {
                case Either.Right<ThreadError, MessageAdd> r ->
                    persist(r.get(), it -> {
                                msg.replyTo().tell(new ThreadAggregateProtocol.AddMessageSucceeded(ULID.newULID(), msg.id(), id));
                                return just((Just)it);
                            }
                    );
                case Either.Left<ThreadError, MessageAdd> l -> {
                    msg.replyTo().tell(new ThreadAggregateProtocol.AddMessageFailed(ULID.newULID(), msg.id(), id, l.getLeft()));
                    yield just(state);
                }
                default -> throw new IllegalStateException("Unexpected value: " + result);
            };
        });
        return builder.build();
    }

    private Receive<ThreadAggregateProtocol.CommandRequest> empty(Empty state) {
        var builder = newReceiveBuilder();
        builder.onMessage(ThreadAggregateProtocol.CreateThread.class, msg ->
                {
                    var result = ThreadFactory.create(id, msg.accountId());
                    return switch (result) {
                        case Either.Right<ThreadError, ThreadCreated> r -> persist(r.get(), it -> {
                                    msg.replyTo().tell(new ThreadAggregateProtocol.CreateThreadSucceeded(ULID.newULID(), msg.id(), id));
                                    return just((Just) it);
                                }
                        );
                        case Either.Left<ThreadError, ThreadCreated> l -> {
                            msg.replyTo().tell(new ThreadAggregateProtocol.CreateThreadFailed(ULID.newULID(), msg.id(), id, l.getLeft()));
                            yield empty(state);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + result);
                    };
                }
        );
        return builder.build();
    }

    private Receive<ThreadAggregateProtocol.CommandRequest> replayHandler() {
        var builder = newReceiveBuilder();
        builder.onMessage(ThreadAggregateProtocol.StateRecoveryCompleted.class, msg ->
                switch (msg.state()) {
                    case Empty e -> stashBuffer.unstashAll(empty(e));
                    case Just j -> stashBuffer.unstashAll(just(j));
                }
        );
        builder.onAnyMessage(msg -> {
            stashBuffer.stash(msg);
            return Behaviors.same();
        });
        return builder.build();
    }


    @Override
    public Receive<ThreadAggregateProtocol.CommandRequest> createReceive() {
        return replayHandler();
    }
}
