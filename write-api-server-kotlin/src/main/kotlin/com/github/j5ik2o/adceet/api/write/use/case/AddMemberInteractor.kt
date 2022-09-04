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

class AddMemberInteractor(
  private val system: ActorSystem<Void>,
  private val threadAggregateRef: ActorRef<ThreadAggregateProtocol.CommandRequest>,
  private val askTimeout: Duration = Duration.ofSeconds(3)
) : AddMemberUseCase {
  override fun execute(threadId: ThreadId, accountId: AccountId): CompletionStage<ThreadId> {
    return AskPattern.ask<ThreadAggregateProtocol.CommandRequest, ThreadAggregateProtocol.AddMemberReply>(
      threadAggregateRef,
      { replyTo -> ThreadAggregateProtocol.AddMember(ULID.newULID(), threadId, accountId, replyTo) },
      askTimeout,
      system.scheduler()
    ).thenCompose { result ->
      when (result) {
        is ThreadAggregateProtocol.AddMemberSucceeded ->
          CompletableFuture.completedStage(result.threadId)
        is ThreadAggregateProtocol.AddMemberFailed ->
          CompletableFuture.failedFuture(AddMemberException(result.error.message))
      }
    }
  }
}
