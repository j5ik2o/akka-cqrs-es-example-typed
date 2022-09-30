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
package com.github.j5ik2o.adceet.domain

import com.github.j5ik2o.adceet.domain.ThreadEvents.{MemberAdd, MessageAdd, ThreadCreated, ThreadEvent}
import wvlet.airframe.ulid.ULID

final case class Thread(id: ThreadId, accountIds: Seq[AccountId], messagesIds: Seq[MessageIdWithAccountId]) {
  def addMember(accountId: AccountId): Either[ThreadError, MemberAdd] = {
    if (accountIds.contains(accountId)) {
      Left(ThreadError.ExistsMemberError(accountId))
    } else {
      Right(MemberAdd(ULID.newULID, id, accountId))
    }
  }

  def addMessage(accountId: AccountId, messageId: MessageId, body: String): Either[ThreadError, MessageAdd] = {
    if (!accountIds.contains(accountId)) {
      Left(ThreadError.NotMemberError(accountId))
    } else {
      Right(MessageAdd(ULID.newULID, id, accountId, messageId, body))
    }
  }

  def updateEvent(threadEvent: ThreadEvent): Thread = {
    threadEvent match {
      case te: MemberAdd =>
        copy(accountIds = accountIds :+ te.accountId)
      case te: MessageAdd =>
        copy(messagesIds = messagesIds :+ MessageIdWithAccountId(te.messageId, te.accountId))
    }
  }
}

object Thread {
  final val MAX_MESSAGE_COUNT = 1000
  def create(id: ThreadId, accountId: AccountId): Either[ThreadError, ThreadCreated] = {
    Right(ThreadCreated(ULID.newULID, id, accountId))
  }

  def applyEvent(event: ThreadCreated): Thread = {
    Thread(event.threadId, Vector(event.accountId), Vector.empty)
  }
}
