package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.ActorRef
import com.github.j5ik2o.adceet.api.write.CborSerializable
import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.ThreadError
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import java.util.UUID

object ThreadAggregateProtocol {
  sealed interface CommandRequest : CborSerializable {
    val id: UUID
    val threadId: ThreadId
  }

  data class CreateThread(
    override val id: UUID,
    override val threadId: ThreadId,
    val accountId: AccountId,
    val replyTo: ActorRef<CreateThreadReply>
  ) : CommandRequest

  sealed interface CreateThreadReply : CborSerializable
  data class CreateThreadSucceeded(val id: UUID, val requestId: UUID, val threadId: ThreadId) : CreateThreadReply
  data class CreateThreadFailed(val id: UUID, val requestId: UUID, val threadId: ThreadId, val error: ThreadError) :
    CreateThreadReply

  data class AddMember(
    override val id: UUID,
    override val threadId: ThreadId,
    val accountId: AccountId,
    val replyTo: ActorRef<AddMemberReply>
  ) :
    CommandRequest

  sealed interface AddMemberReply : CborSerializable
  data class AddMemberSucceeded(val id: UUID, val requestId: UUID, val threadId: ThreadId) : AddMemberReply
  data class AddMemberFailed(val id: UUID, val requestId: UUID, val threadId: ThreadId, val error: ThreadError) :
    AddMemberReply

  data class AddMessage(
    override val id: UUID,
    override val threadId: ThreadId,
    val message: Message,
    val replyTo: ActorRef<AddMessageReply>
  ) : CommandRequest
  sealed interface AddMessageReply : CborSerializable
  data class AddMessageSucceeded(val id: UUID, val requestId: UUID, val threadId: ThreadId) : AddMessageReply
  data class AddMessageFailed(val id: UUID, val requestId: UUID, val threadId: ThreadId, val error: ThreadError) :
    AddMessageReply

  internal object Idle : CommandRequest {
    override val id: UUID = UUID.randomUUID()
    override val threadId: ThreadId by lazy { throw UnsupportedOperationException() }
  }

  object Stop : CommandRequest {
    override val id: UUID = UUID.randomUUID()
    override val threadId: ThreadId by lazy { throw UnsupportedOperationException() }
  }

  data class WrappedPersistReply(
    override val id: UUID,
    override val threadId: ThreadId,
    val message: ThreadPersist.PersistCompleted
  ) : CommandRequest

  data class StateRecoveryCompleted(
    override val id: UUID,
    override val threadId: ThreadId,
    val state: ThreadState
  ) : CommandRequest
}
