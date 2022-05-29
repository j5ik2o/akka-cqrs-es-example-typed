package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.actor.typed.javadsl.StashBuffer
import arrow.core.Either
import com.github.j5ik2o.adceet.api.write.domain.Thread
import com.github.j5ik2o.adceet.api.write.domain.ThreadEvent
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import java.util.UUID

class ThreadAggregate(
  private val ctx: ActorContext<ThreadAggregateProtocol.CommandRequest>,
  private val stashBuffer: StashBuffer<ThreadAggregateProtocol.CommandRequest>,
  private val id: ThreadId,
  persistBehaviorF: (ThreadId, ActorRef<ThreadAggregateProtocol.CommandRequest>) -> Behavior<ThreadPersist.Persist>
) :
  AbstractBehavior<ThreadAggregateProtocol.CommandRequest>(ctx) {

  private val persistRef: ActorRef<ThreadPersist.Persist> = ctx.spawn(persistBehaviorF(id, ctx.self), "persist")

  private fun persist(
    threadEvent: ThreadEvent,
    succ: (ThreadState) -> Behavior<ThreadAggregateProtocol.CommandRequest>
  ): Behavior<ThreadAggregateProtocol.CommandRequest> {
    val messageAdaptor = ctx.messageAdapter(ThreadPersist.PersistCompleted::class.java) { msg ->
      ThreadAggregateProtocol.WrappedPersistReply(UUID.randomUUID(), id, msg)
    }
    persistRef.tell(ThreadPersist.Persist(threadEvent, messageAdaptor))
    return Behaviors.receiveMessage { msg ->
      when (msg) {
        is ThreadAggregateProtocol.WrappedPersistReply ->
          succ(msg.message.state)
        else ->
          Behaviors.unhandled()
      }
    }
  }

  private fun just(state: ThreadState.Companion.Just): Receive<ThreadAggregateProtocol.CommandRequest> {
    val builder = newReceiveBuilder()
    builder.onMessage(ThreadAggregateProtocol.AddMember::class.java) { msg ->
      when (val result = state.thread.addMember(msg.accountId)) {
        is Either.Right ->
          persist(result.value) {
            msg.replyTo.tell(ThreadAggregateProtocol.AddMemberSucceeded(UUID.randomUUID(), msg.id, id))
            just(it as ThreadState.Companion.Just)
          }
        is Either.Left -> {
          msg.replyTo.tell(
            ThreadAggregateProtocol.AddMemberFailed(
              UUID.randomUUID(),
              msg.id,
              id,
              result.value
            )
          )
          just(state)
        }
      }
    }
    builder.onMessage(ThreadAggregateProtocol.AddMessage::class.java) { msg ->
      when (val result = state.thread.addMessage(msg.message.senderId, msg.message.id, msg.message.body)) {
        is Either.Right ->
          persist(result.value) {
            msg.replyTo.tell(ThreadAggregateProtocol.AddMessageSucceeded(UUID.randomUUID(), msg.id, id))
            just(it as ThreadState.Companion.Just)
          }
        is Either.Left -> {
          msg.replyTo.tell(
            ThreadAggregateProtocol.AddMessageFailed(
              UUID.randomUUID(),
              msg.id,
              id,
              result.value
            )
          )
          just(state)
        }
      }
    }
    return builder.build()
  }

  private fun empty(state: ThreadState.Companion.Empty): Receive<ThreadAggregateProtocol.CommandRequest> {
    val builder = newReceiveBuilder()
    builder.onMessage(ThreadAggregateProtocol.CreateThread::class.java) { msg ->
      when (val result = Thread.create(id, msg.accountId)) {
        is Either.Right ->
          persist(result.value) {
            msg.replyTo.tell(ThreadAggregateProtocol.CreateThreadSucceeded(UUID.randomUUID(), msg.id, id))
            just(it as ThreadState.Companion.Just)
          }
        is Either.Left -> {
          msg.replyTo.tell(
            ThreadAggregateProtocol.CreateThreadFailed(
              UUID.randomUUID(),
              msg.id,
              id,
              result.value
            )
          )
          empty(state)
        }
      }
    }
    return builder.build()
  }

  private fun replayHandler(): Receive<ThreadAggregateProtocol.CommandRequest> {
    val builder = newReceiveBuilder()
    builder.onMessage(ThreadAggregateProtocol.StateRecoveryCompleted::class.java) { msg ->
      when (val state = msg.state) {
        is ThreadState.Companion.Empty ->
          stashBuffer.unstashAll(empty(state))
        is ThreadState.Companion.Just ->
          stashBuffer.unstashAll(just(state))
      }
    }
    builder.onAnyMessage { msg ->
      stashBuffer.stash(msg)
      Behaviors.same()
    }
    return builder.build()
  }

  override fun createReceive(): Receive<ThreadAggregateProtocol.CommandRequest> {
    return replayHandler()
  }

  companion object {

    fun create(
      id: ThreadId,
      persistBehaviorF: (ThreadId, ActorRef<ThreadAggregateProtocol.CommandRequest>) -> Behavior<ThreadPersist.Persist> = { id, self ->
        ThreadPersist.persistBehavior(
          id,
          self
        )
      }
    ): Behavior<ThreadAggregateProtocol.CommandRequest> = Behaviors.setup { ctx ->
      Behaviors.withStash(255) { stashBuffer ->
        ThreadAggregate(ctx, stashBuffer, id, persistBehaviorF)
      }
    }
  }
}
