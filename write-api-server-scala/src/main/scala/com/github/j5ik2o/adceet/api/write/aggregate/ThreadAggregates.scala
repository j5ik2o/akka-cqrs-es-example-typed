package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.j5ik2o.adceet.api.write.domain.ThreadId

object ThreadAggregates {
  final val name = "threads"

  def create(nameF: ThreadId => String)(
      childBehaviorF: ThreadId => Behavior[ThreadAggregateProtocol.CommandRequest]
  ): Behavior[ThreadAggregateProtocol.CommandRequest] = {
    Behaviors.setup { ctx =>
      def getOrCreateRef(threadId: ThreadId): ActorRef[ThreadAggregateProtocol.CommandRequest] = {
        ctx.child(nameF(threadId)) match {
          case Some(ref) =>
            ref.unsafeUpcast
          case None =>
            ctx.spawn(childBehaviorF(threadId), nameF(threadId))
        }
      }
      Behaviors.receiveMessage { msg =>
        getOrCreateRef(msg.threadId) ! msg
        Behaviors.same
      }
    }
  }
}
