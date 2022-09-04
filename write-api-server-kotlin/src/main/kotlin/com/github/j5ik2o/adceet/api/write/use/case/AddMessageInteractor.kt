package com.github.j5ik2o.adceet.api.write.use.case

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.AskPattern
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadAggregateProtocol
import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import wvlet.airframe.ulid.ULID
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class AddMessageInteractor(
  private val system: ActorSystem<Void>,
  private val threadAggregateRef: ActorRef<ThreadAggregateProtocol.CommandRequest>,
  private val askTimeout: Duration = Duration.ofSeconds(3)
) : AddMessageUseCase {
  override fun execute(message: Message): CompletionStage<ThreadId> {
    return AskPattern.ask(
      threadAggregateRef,
      { replyTo -> ThreadAggregateProtocol.AddMessage(ULID.newULID(), message.threadId, message, replyTo) },
      askTimeout,
      system.scheduler()
    ).thenCompose { result ->
      when (result) {
        is ThreadAggregateProtocol.AddMessageSucceeded ->
          CompletableFuture.completedStage(result.threadId)
        is ThreadAggregateProtocol.AddMessageFailed ->
          CompletableFuture.failedFuture(AddMessageException(result.error.message))
      }
    }
  }
}
