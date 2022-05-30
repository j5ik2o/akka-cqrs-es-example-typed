package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import akka.persistence.typed.{ PersistenceId, RecoveryCompleted }
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import com.github.j5ik2o.adceet.api.write.aggregate.ThreadAggregateProtocol.StateRecoveryCompleted
import com.github.j5ik2o.adceet.api.write.domain.ThreadEvents.ThreadEvent
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import wvlet.airframe.ulid.ULID

object ThreadPersist {
  final case class Persist(threadEvent: ThreadEvent, replyTo: ActorRef[PersistCompleted])
  final case class PersistCompleted(state: ThreadState)

  def persistBehavior(id: ThreadId, parentRef: ActorRef[ThreadAggregateProtocol.CommandRequest]): Behavior[Persist] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior(
        PersistenceId.ofUniqueId(id.asString),
        emptyState = ThreadState.Empty(id),
        commandHandler = { (state: ThreadState, command: Persist) =>
          (state, command) match {
            case (_, Persist(event, replyTo)) =>
              Effect.persist(event).thenReply(replyTo) { state: ThreadState =>
                PersistCompleted(state)
              }
          }
        },
        eventHandler = { (s: ThreadState, event: ThreadEvent) =>
          s.applyEvent(event)
        }
      ).receiveSignal { case (state, RecoveryCompleted) =>
        parentRef ! StateRecoveryCompleted(ULID.newULID, id, state)
      }
    }

}
