package com.github.j5ik2o.adceet.api.write.domain

import wvlet.airframe.ulid.ULID

final case class Thread(id: ThreadId, accountIds: Seq[AccountId], messagesIds: Seq[MessageIdWithAccountId])

object Thread {
  final val MAX_MESSAGE_COUNT = 1000
  def create(id: ThreadId, accountId: AccountId): Either[ThreadError, ThreadCreated] = {
    Right(ThreadCreated(ULID.newULID, id, accountId))
  }

  def applyEvent(event: ThreadCreated): Thread = {
    Thread(event.threadId, Vector(event.accountId), Vector.empty)
  }
}
