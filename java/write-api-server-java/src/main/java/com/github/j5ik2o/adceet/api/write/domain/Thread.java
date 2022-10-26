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
package com.github.j5ik2o.adceet.api.write.domain;

import com.github.j5ik2o.adceet.api.write.domain.errors.ExistsMemberError;
import com.github.j5ik2o.adceet.api.write.domain.errors.NotMemberError;
import com.github.j5ik2o.adceet.api.write.domain.errors.ThreadError;
import com.github.j5ik2o.adceet.api.write.domain.events.MemberAdd;
import com.github.j5ik2o.adceet.api.write.domain.events.MessageAdd;
import com.github.j5ik2o.adceet.api.write.domain.events.ThreadEvent;
import io.vavr.collection.Seq;
import io.vavr.control.Either;

import java.util.UUID;

public record Thread(ThreadId id, Seq<AccountId> accountIds, Seq<MessageIdWithAccountId> messageIds) {

    public Either<ThreadError, MemberAdd> addMember(AccountId accountId) {
        if (accountIds.contains(accountId)) {
            return Either.left(new ExistsMemberError(accountId));
        } else {
            return Either.right(new MemberAdd(UUID.randomUUID(), id, accountId));
        }
    }

    public Either<ThreadError, MessageAdd> addMessage(AccountId accountId, MessageId messageId, String body) {
        if (!accountIds.contains(accountId)) {
            return Either.left(new NotMemberError(accountId));
        } else {
            return Either.right(new MessageAdd(UUID.randomUUID(), id, accountId, messageId, body));
        }
    }

    public Thread updateEvent(ThreadEvent threadEvent) {
        return switch (threadEvent) {
            case MemberAdd a -> new Thread(this.id, this.accountIds.append(a.accountId()), this.messageIds);
            case MessageAdd a ->
                    new Thread(this.id, this.accountIds, this.messageIds.append(new MessageIdWithAccountId(a.messageId(), a.accountId())));
            default -> throw new IllegalStateException("Unexpected value: " + threadEvent);
        };
    }

}
