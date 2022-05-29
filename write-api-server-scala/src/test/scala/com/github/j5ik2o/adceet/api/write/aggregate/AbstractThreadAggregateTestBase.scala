package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.j5ik2o.adceet.api.write.domain.{ AccountId, ThreadId }
import wvlet.airframe.ulid.ULID

abstract class AbstractThreadAggregateTestBase(testKit: ActorTestKit) {
  val inMemoryMode: Boolean = false

  def behavior(id: ThreadId, inMemoryMode: Boolean): Behavior[ThreadAggregateProtocol.CommandRequest]

  private def actorRef(id: ThreadId, inMemoryMode: Boolean): ActorRef[ThreadAggregateProtocol.CommandRequest] = {
    testKit.spawn(behavior(id, inMemoryMode))
  }

  def shouldCreateThread(): Unit = {
    val id        = ThreadId()
    val threadRef = actorRef(id, inMemoryMode)
    val accountId = AccountId()

    val createThreadReplyProbe = testKit.createTestProbe[ThreadAggregateProtocol.CreateThreadReply]()
    threadRef.tell(
      ThreadAggregateProtocol.CreateThread(
        ULID.newULID,
        id,
        accountId,
        createThreadReplyProbe.ref
      )
    )
    val createThreadReply = createThreadReplyProbe.expectMessageType[ThreadAggregateProtocol.CreateThreadSucceeded]
    assert(id == createThreadReply.threadId)
  }

}
