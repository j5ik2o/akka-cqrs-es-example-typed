package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.RecoveryCompleted
import akka.persistence.typed.javadsl.CommandHandler
import akka.persistence.typed.javadsl.EventHandler
import akka.persistence.typed.javadsl.EventSourcedBehavior
import akka.persistence.typed.javadsl.SignalHandler
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.github.j5ik2o.adceet.api.write.domain.ThreadEvent
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import wvlet.airframe.ulid.ULID
import java.util.concurrent.ConcurrentHashMap

object ThreadPersist {
  data class Persist(val threadEvent: ThreadEvent, val replyTo: ActorRef<PersistCompleted>)
  data class PersistCompleted(val state: ThreadState)

  private val persistStates = ConcurrentHashMap<ThreadId, Option<ThreadState>>()

  fun dispose() {
    persistStates.clear()
  }

  fun persistInMemoryBehavior(
    id: ThreadId,
    parentRef: ActorRef<ThreadAggregateProtocol.CommandRequest>
  ): Behavior<Persist> {
    return Behaviors.setup {
      val initialState = when (val ps = persistStates.getOrDefault(id, None)) {
        is Some -> {
          parentRef.tell(
            ThreadAggregateProtocol.StateRecoveryCompleted(
              ULID.newULID(),
              id,
              ps.value
            )
          )
          ps.value
        }
        is None -> {
          val state = ThreadState.Companion.Empty(id)
          parentRef.tell(
            ThreadAggregateProtocol.StateRecoveryCompleted(
              ULID.newULID(),
              id,
              state
            )
          )
          state
        }
      }
      fun state(s: ThreadState): Behavior<Persist> {
        return Behaviors.receiveMessage { msg ->
          when (msg) {
            is Persist -> {
              val newState = s.applyEvent(msg.threadEvent)
              msg.replyTo.tell(PersistCompleted(newState))
              persistStates[id] = Some(newState)
              state(newState)
            }
            else -> {
              Behaviors.same()
            }
          }
        }
      }
      state(initialState)
    }
  }

  internal class PersistentActor(
    private val ctx: ActorContext<Persist>,
    private val id: ThreadId,
    private val parentRef: ActorRef<ThreadAggregateProtocol.CommandRequest>
  ) :
    EventSourcedBehavior<Persist, ThreadEvent, ThreadState>(
      PersistenceId.ofUniqueId(id.asString())
    ) {
    override fun signalHandler(): SignalHandler<ThreadState> {
      val builder = newSignalHandlerBuilder()
      builder.onSignal(RecoveryCompleted::class.java) { state, _ ->
        parentRef.tell(ThreadAggregateProtocol.StateRecoveryCompleted(ULID.newULID(), id, state))
      }
      return builder.build()
    }

    override fun commandHandler(): CommandHandler<Persist, ThreadEvent, ThreadState> {
      val builder = newCommandHandlerBuilder()
      builder.forAnyState().onCommand(Persist::class.java) { cmd ->
        Effect().persist(cmd.threadEvent).thenReply(cmd.replyTo) {
          PersistCompleted(it)
        }
      }
      return builder.build()
    }

    override fun eventHandler(): EventHandler<ThreadState, ThreadEvent> {
      val builder = newEventHandlerBuilder()
      builder.forAnyState().onAnyEvent { s, e ->
        s.applyEvent(e)
      }
      return builder.build()
    }

    override fun emptyState(): ThreadState {
      return ThreadState.Companion.Empty(id)
    }
  }

  fun persistBehavior(id: ThreadId, parentRef: ActorRef<ThreadAggregateProtocol.CommandRequest>): Behavior<Persist> {
    return Behaviors.setup { ctx ->
      PersistentActor(ctx, id, parentRef)
    }
  }
}
