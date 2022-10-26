package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.ActorRef
import com.github.j5ik2o.adceet.api.write.CborSerializable
import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.ThreadError
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import wvlet.airframe.ulid.ULID

object ThreadAggregateProtocol {
  sealed interface CommandRequest : CborSerializable {
    val id: ULID
    val threadId: ThreadId
  }

  data class CreateThread(
    override val id: ULID,
    override val threadId: ThreadId,
    val accountId: AccountId,
    val replyTo: ActorRef<CreateThreadReply>
  ) : CommandRequest

  sealed interface CreateThreadReply : CborSerializable
  data class CreateThreadSucceeded(val id: ULID, val requestId: ULID, val threadId: ThreadId) : CreateThreadReply
  data class CreateThreadFailed(val id: ULID, val requestId: ULID, val threadId: ThreadId, val error: ThreadError) :
    CreateThreadReply

  data class AddMember(
    override val id: ULID,
    override val threadId: ThreadId,
    val accountId: AccountId,
    val replyTo: ActorRef<AddMemberReply>
  ) :
    CommandRequest

  sealed interface AddMemberReply : CborSerializable
  data class AddMemberSucceeded(val id: ULID, val requestId: ULID, val threadId: ThreadId) : AddMemberReply
  data class AddMemberFailed(val id: ULID, val requestId: ULID, val threadId: ThreadId, val error: ThreadError) :
    AddMemberReply

  data class AddMessage(
    override val id: ULID,
    override val threadId: ThreadId,
    val message: Message,
    val replyTo: ActorRef<AddMessageReply>
  ) : CommandRequest
  sealed interface AddMessageReply : CborSerializable
  data class AddMessageSucceeded(val id: ULID, val requestId: ULID, val threadId: ThreadId) : AddMessageReply
  data class AddMessageFailed(val id: ULID, val requestId: ULID, val threadId: ThreadId, val error: ThreadError) :
    AddMessageReply

  internal object Idle : CommandRequest {
    override val id: ULID = ULID.newULID()
    override val threadId: ThreadId by lazy { throw UnsupportedOperationException() }
  }

  object Stop : CommandRequest {
    override val id: ULID = ULID.newULID()
    override val threadId: ThreadId by lazy { throw UnsupportedOperationException() }
  }

  data class WrappedPersistReply(
    override val id: ULID,
    override val threadId: ThreadId,
    val message: ThreadPersist.PersistCompleted
  ) : CommandRequest

  data class StateRecoveryCompleted(
    override val id: ULID,
    override val threadId: ThreadId,
    val state: ThreadState
  ) : CommandRequest
}
