package com.github.j5ik2o.adceet.api.write.use.case

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.AskPattern
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadAggregateProtocol
import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import wvlet.airframe.ulid.ULID
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class CreateThreadInteractor(
  private val system: ActorSystem<Void>,
  private val threadAggregateRef: ActorRef<ThreadAggregateProtocol.CommandRequest>,
  private val askTimeout: Duration = Duration.ofSeconds(30)
) : CreateThreadUseCase {
  override fun execute(threadId: ThreadId, accountId: AccountId): CompletionStage<ThreadId> {
    return AskPattern.ask<ThreadAggregateProtocol.CommandRequest, ThreadAggregateProtocol.CreateThreadReply>(
      threadAggregateRef,
      { replyTo -> ThreadAggregateProtocol.CreateThread(ULID.newULID(), threadId, accountId, replyTo) },
      askTimeout,
      system.scheduler()
    ).thenCompose { result ->
      when (result) {
        is ThreadAggregateProtocol.CreateThreadSucceeded ->
          CompletableFuture.completedStage(result.threadId)
        is ThreadAggregateProtocol.CreateThreadFailed ->
          CompletableFuture.failedFuture(CreateThreadException(result.error.message))
      }
    }
  }
}
