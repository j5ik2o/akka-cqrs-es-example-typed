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

import akka.actor.typed.scaladsl.Behaviors.Receive
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors, StashBuffer }
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.j5ik2o.adceet.api.write.domain.ThreadEvents.ThreadEvent
import com.github.j5ik2o.adceet.api.write.domain.{ Thread, ThreadId }
import wvlet.airframe.ulid.ULID

final class ThreadAggregate private (
    private val ctx: ActorContext[ThreadAggregateProtocol.CommandRequest],
    private val stashBuffer: StashBuffer[ThreadAggregateProtocol.CommandRequest],
    private val id: ThreadId,
    persistBehaviorF: (ThreadId, ActorRef[ThreadAggregateProtocol.CommandRequest]) => Behavior[ThreadPersist.Persist]
) extends AbstractBehavior[ThreadAggregateProtocol.CommandRequest](ctx) {

  private val persistRef = ctx.spawn(persistBehaviorF(id, ctx.self), "persist")

  private def persist(threadEvent: ThreadEvent)(
      succ: (ThreadState) => Behavior[ThreadAggregateProtocol.CommandRequest]
  ): Behavior[ThreadAggregateProtocol.CommandRequest] = {
    val messageAdaptor = ctx.messageAdapter { msg =>
      ThreadAggregateProtocol.WrappedPersistReply(ULID.newULID, id, msg)
    }
    persistRef ! ThreadPersist.Persist(threadEvent, messageAdaptor)
    Behaviors.receiveMessage {
      case msg: ThreadAggregateProtocol.WrappedPersistReply =>
        succ(msg.message.state)
      case _ =>
        Behaviors.unhandled
    }
  }

  private def just(state: ThreadState.Just): Receive[ThreadAggregateProtocol.CommandRequest] = {
    Behaviors.receiveMessage {
      case msg: ThreadAggregateProtocol.AddMember =>
        state.thread.addMember(msg.accountId) match {
          case Right(event) =>
            persist(event) { it =>
              msg.replyTo ! ThreadAggregateProtocol.AddMemberSucceeded(ULID.newULID, msg.id, id)
              just(it.asInstanceOf[ThreadState.Just])
            }
          case Left(err) =>
            msg.replyTo ! ThreadAggregateProtocol.AddMemberFailed(
              ULID.newULID,
              msg.id,
              id,
              err
            )
            just(state)
        }
      case msg: ThreadAggregateProtocol.AddMessage =>
        state.thread.addMessage(msg.message.senderId, msg.message.id, msg.message.body) match {
          case Right(event) =>
            persist(event) { it =>
              msg.replyTo ! ThreadAggregateProtocol.AddMessageSucceeded(ULID.newULID, msg.id, id)
              just(it.asInstanceOf[ThreadState.Just])
            }
          case Left(err) =>
            msg.replyTo !
            ThreadAggregateProtocol.AddMessageFailed(
              ULID.newULID,
              msg.id,
              id,
              err
            )
            just(state)
        }
    }
  }

  private def empty(state: ThreadState.Empty): Receive[ThreadAggregateProtocol.CommandRequest] = {
    Behaviors.receiveMessage { case msg: ThreadAggregateProtocol.CreateThread =>
      Thread.create(id, msg.accountId) match {
        case Right(event) =>
          persist(event) { it =>
            msg.replyTo ! ThreadAggregateProtocol.CreateThreadSucceeded(ULID.newULID, msg.id, id)
            just(it.asInstanceOf[ThreadState.Just])
          }
        case Left(err) =>
          msg.replyTo !
          ThreadAggregateProtocol.CreateThreadFailed(
            ULID.newULID,
            msg.id,
            id,
            err
          )
          empty(state)
      }
    }
  }

  override def onMessage(
      msg: ThreadAggregateProtocol.CommandRequest
  ): Behavior[ThreadAggregateProtocol.CommandRequest] = {
    msg match {
      case msg: ThreadAggregateProtocol.StateRecoveryCompleted =>
        msg.state match {
          case state: ThreadState.Empty =>
            stashBuffer.unstashAll(empty(state))
          case state: ThreadState.Just =>
            stashBuffer.unstashAll(just(state))
        }
      case msg =>
        stashBuffer.stash(msg)
        Behaviors.same
    }
  }
}

object ThreadAggregate {

  def create(id: ThreadId)(
      persistBehaviorF: (
          ThreadId,
          ActorRef[ThreadAggregateProtocol.CommandRequest]
      ) => Behavior[ThreadPersist.Persist] = { (id, self) =>
        ThreadPersist.persistBehavior(
          id,
          self
        )
      }
  ): Behavior[ThreadAggregateProtocol.CommandRequest] = Behaviors.setup { ctx =>
    Behaviors.withStash(255) { stashBuffer =>
      new ThreadAggregate(ctx, stashBuffer, id, persistBehaviorF)
    }
  }

}
