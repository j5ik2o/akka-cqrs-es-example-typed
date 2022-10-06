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

import com.github.j5ik2o.adceet.infrastructure.serde.CborSerializable
import wvlet.airframe.ulid.ULID

import java.time.Instant

object ThreadEvents {

  trait ThreadEvent extends CborSerializable {
    def id: ULID
    def threadId: ThreadId
  }

  final case class ThreadCreated(id: ULID, threadId: ThreadId, accountId: AccountId, occurredAt: Instant)
      extends ThreadEvent
  final case class MemberAdded(id: ULID, threadId: ThreadId, accountId: AccountId, occurredAt: Instant)
      extends ThreadEvent
  final case class MessageAdded(
      id: ULID,
      threadId: ThreadId,
      accountId: AccountId,
      messageId: MessageId,
      body: String,
      occurredAt: Instant
  ) extends ThreadEvent

}
