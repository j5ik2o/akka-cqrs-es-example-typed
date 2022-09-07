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

import com.github.j5ik2o.adceet.api.write.CborSerializable
import java.util.UUID

sealed interface ThreadEvent : CborSerializable {
  val id: UUID
  val threadId: ThreadId
}

data class ThreadCreated(override val id: UUID, override val threadId: ThreadId, val accountId: AccountId) : ThreadEvent
data class MemberAdd(override val id: UUID, override val threadId: ThreadId, val accountId: AccountId) : ThreadEvent
data class MessageAdd(
  override val id: UUID,
  override val threadId: ThreadId,
  val accountId: AccountId,
  val messageId: MessageId,
  val body: String
) :
  ThreadEvent
