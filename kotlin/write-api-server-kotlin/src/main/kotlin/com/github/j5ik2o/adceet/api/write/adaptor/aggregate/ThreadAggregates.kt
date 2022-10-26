package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import com.github.j5ik2o.adceet.api.write.domain.ThreadId

object ThreadAggregates {
  const val name = "threads"

  fun create(
    nameF: (ThreadId) -> String,
    childBehaviorF: (ThreadId) -> Behavior<ThreadAggregateProtocol.CommandRequest>
  ): Behavior<ThreadAggregateProtocol.CommandRequest> {
    return Behaviors.setup { ctx ->
      fun getOrCreateRef(threadId: ThreadId): ActorRef<ThreadAggregateProtocol.CommandRequest> {
        val childRef = ctx.getChild(nameF(threadId))
        return if (childRef.isEmpty) {
          ctx.spawn(childBehaviorF(threadId), nameF(threadId))
        } else {
          childRef.get().unsafeUpcast()
        }
      }
      Behaviors.receiveMessage { msg ->
        getOrCreateRef(msg.threadId).tell(msg)
        Behaviors.same()
      }
    }
  }
}
