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
package com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol;

import akka.actor.typed.ActorRef;
import com.github.j5ik2o.adceet.api.write.CborSerializable;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadPersist;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.ThreadState;
import com.github.j5ik2o.adceet.api.write.domain.AccountId;
import com.github.j5ik2o.adceet.api.write.domain.Message;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.github.j5ik2o.adceet.api.write.domain.errors.ThreadError;
import wvlet.airframe.ulid.ULID;

public final class ThreadAggregateProtocol {
    public sealed interface CommandRequest extends CborSerializable {
        ULID id();

        ThreadId threadId();
    }

    public record CreateThread(
            ULID id,
            ThreadId threadId,
            AccountId accountId,
            ActorRef<CreateThreadReply> replyTo
    ) implements CommandRequest {
    }

    public sealed interface CreateThreadReply extends CborSerializable {
        ULID id();

        ULID requestId();

        ThreadId threadId();
    }

    public record CreateThreadSucceeded(ULID id, ULID requestId, ThreadId threadId) implements CreateThreadReply {
    }

    public record CreateThreadFailed(ULID id, ULID requestId, ThreadId threadId,
                                     ThreadError error) implements CreateThreadReply {
    }

    public record AddMember(
            ULID id,
            ThreadId threadId,
            AccountId accountId,
            ActorRef<AddMemberReply> replyTo
    ) implements
            CommandRequest {
    }

    public sealed interface AddMemberReply extends CborSerializable {
    }

    public record AddMemberSucceeded(ULID id, ULID requestId, ThreadId threadId) implements AddMemberReply {
    }

    public record AddMemberFailed(ULID id, ULID requestId, ThreadId threadId, ThreadError error) implements
            AddMemberReply {
    }


    public record AddMessage(
            ULID id,
            ThreadId threadId,
            Message message,
            ActorRef<AddMessageReply> replyTo
    ) implements CommandRequest {
    }

    public sealed interface AddMessageReply extends CborSerializable {
    }

    public record AddMessageSucceeded(ULID id, ULID requestId, ThreadId threadId) implements AddMessageReply {
    }

    public record AddMessageFailed(ULID id, ULID requestId, ThreadId threadId, ThreadError error) implements
            AddMessageReply {
    }

    public static final class Idle implements CommandRequest {
        private final ULID id;

        public Idle() {
            this.id = ULID.newULID();
        }

        @Override
        public ULID id() {
            return id;
        }

        @Override
        public ThreadId threadId() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class Stop implements CommandRequest {
        private final ULID id;

        public Stop() {
            this.id = ULID.newULID();
        }

        @Override
        public ULID id() {
            return id;
        }

        @Override
        public ThreadId threadId() {
            throw new UnsupportedOperationException();
        }
    }

    public record WrappedPersistReply(
            ULID id,
            ThreadId threadId,
            ThreadPersist.PersistCompleted message
    ) implements CommandRequest {
    }

    public record StateRecoveryCompleted(
            ULID id,
            ThreadId threadId,
            ThreadState state
    ) implements CommandRequest {
    }

}