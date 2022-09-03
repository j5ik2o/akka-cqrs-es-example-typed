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
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.state.ThreadState;
import com.github.j5ik2o.adceet.api.write.domain.AccountId;
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

    public record StateRecoveryCompleted(
            ULID id,
            ThreadId threadId,
            ThreadState state
    ) implements CommandRequest {
    }

}
