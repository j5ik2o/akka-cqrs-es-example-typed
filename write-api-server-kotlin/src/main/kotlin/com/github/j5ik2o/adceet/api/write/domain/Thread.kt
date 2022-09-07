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
package com.github.j5ik2o.adceet.api.write.domain

import arrow.core.Either
import io.vavr.collection.Seq
import io.vavr.collection.Vector
import java.util.UUID

data class Thread(val id: ThreadId, val accountIds: Seq<AccountId>, val messagesIds: Seq<MessageIdWithAccountId>) {

  companion object {
    const val MAX_MESSAGE_COUNT = 1000
    fun create(id: ThreadId, accountId: AccountId): Either<ThreadError, ThreadCreated> {
      return Either.Right(ThreadCreated(UUID.randomUUID(), id, accountId))
    }

    fun applyEvent(event: ThreadCreated): Thread {
      return Thread(event.threadId, Vector.of(event.accountId), Vector.empty())
    }
  }

  fun addMember(accountId: AccountId): Either<ThreadError, MemberAdd> {
    return if (accountIds.contains(accountId)) {
      Either.Left(ThreadError.Companion.ExistsMemberError(accountId))
    } else {
      Either.Right(MemberAdd(UUID.randomUUID(), id, accountId))
    }
  }

  fun addMessage(accountId: AccountId, messageId: MessageId, body: String): Either<ThreadError, MessageAdd> {
    return if (!accountIds.contains(accountId)) {
      Either.Left(ThreadError.Companion.NotMemberError(accountId))
    } else {
      Either.Right(MessageAdd(UUID.randomUUID(), id, accountId, messageId, body))
    }
  }

  fun updateEvent(threadEvent: ThreadEvent): Thread {
    return when (threadEvent) {
      is MemberAdd ->
        copy(accountIds = accountIds.append(threadEvent.accountId))
      is MessageAdd ->
        copy(messagesIds = messagesIds.append(MessageIdWithAccountId(threadEvent.messageId, threadEvent.accountId)))
      else -> {
        throw java.lang.IllegalArgumentException()
      }
    }
  }
}
