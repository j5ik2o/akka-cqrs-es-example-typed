/*
 * Copyright 2022 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.github.j5ik2o.adceet.api.write.aggregate.ThreadAggregateProtocol.StateRecoveryCompleted
import com.github.j5ik2o.adceet.domain.ThreadEvents.ThreadEvent
import com.github.j5ik2o.adceet.domain.ThreadId
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
