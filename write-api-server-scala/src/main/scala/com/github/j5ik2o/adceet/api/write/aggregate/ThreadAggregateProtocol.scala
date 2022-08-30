package com.github.j5ik2o.adceet.api.write.aggregate
import akka.actor.typed.ActorRef
import com.github.j5ik2o.adceet.api.write.CborSerializable
import com.github.j5ik2o.adceet.api.write.domain.{ AccountId, Message, ThreadError, ThreadId }
import wvlet.airframe.ulid.ULID

object ThreadAggregateProtocol {
  sealed trait CommandRequest extends CborSerializable with AggregateIdValueExtractable {
    def id: ULID
    def threadId: ThreadId
    def aggregateIdValue: String = threadId.asString
  }

  final case class CreateThread(
      id: ULID,
      threadId: ThreadId,
      accountId: AccountId,
      replyTo: ActorRef[CreateThreadReply]
  ) extends CommandRequest
  sealed trait CreateThreadReply extends CborSerializable

  case class CreateThreadSucceeded(val id: ULID, val requestId: ULID, val threadId: ThreadId) extends CreateThreadReply
  case class CreateThreadFailed(val id: ULID, val requestId: ULID, val threadId: ThreadId, val error: ThreadError)
      extends CreateThreadReply

  case class AddMember(
      id: ULID,
      threadId: ThreadId,
      accountId: AccountId,
      replyTo: ActorRef[AddMemberReply]
  ) extends CommandRequest
  sealed trait AddMemberReply extends CborSerializable
  case class AddMemberSucceeded(id: ULID, requestId: ULID, threadId: ThreadId) extends AddMemberReply
  case class AddMemberFailed(id: ULID, requestId: ULID, threadId: ThreadId, error: ThreadError) extends AddMemberReply

  case class AddMessage(
      id: ULID,
      threadId: ThreadId,
      message: Message,
      replyTo: ActorRef[AddMessageReply]
  ) extends CommandRequest
  sealed trait AddMessageReply extends CborSerializable
  case class AddMessageSucceeded(id: ULID, requestId: ULID, threadId: ThreadId) extends AddMessageReply
  case class AddMessageFailed(id: ULID, requestId: ULID, threadId: ThreadId, val error: ThreadError)
      extends AddMessageReply

  object Idle extends CommandRequest {
    override val id: ULID           = ULID.newULID
    override def threadId: ThreadId = { throw new UnsupportedOperationException() }
  }

  object Stop extends CommandRequest {
    override val id: ULID           = ULID.newULID
    override def threadId: ThreadId = { throw new UnsupportedOperationException() }
  }

  case class WrappedPersistReply(
      id: ULID,
      threadId: ThreadId,
      message: ThreadPersist.PersistCompleted
  ) extends CommandRequest

  case class StateRecoveryCompleted(
      id: ULID,
      threadId: ThreadId,
      state: ThreadState
  ) extends CommandRequest
}
