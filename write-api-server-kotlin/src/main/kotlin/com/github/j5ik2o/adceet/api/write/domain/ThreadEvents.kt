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
