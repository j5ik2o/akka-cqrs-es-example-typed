package com.github.j5ik2o.adceet.api.write.domain

import com.github.j5ik2o.adceet.api.write.CborSerializable
import wvlet.airframe.ulid.ULID

object ThreadEvents {

  trait ThreadEvent extends CborSerializable {
    def id: ULID
    def threadId: ThreadId
  }

  final case class ThreadCreated(id: ULID, threadId: ThreadId, accountId: AccountId) extends ThreadEvent
  final case class MemberAdd(id: ULID, threadId: ThreadId, accountId: AccountId) extends ThreadEvent
  final case class MessageAdd(id: ULID, threadId: ThreadId, accountId: AccountId, messageId: MessageId, body: String)
      extends ThreadEvent

}
