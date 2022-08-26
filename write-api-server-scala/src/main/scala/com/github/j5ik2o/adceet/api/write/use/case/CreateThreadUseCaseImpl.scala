package com.github.j5ik2o.adceet.api.write.use.`case`

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem, Scheduler }
import akka.util.Timeout
import com.github.j5ik2o.adceet.api.write.aggregate.ThreadAggregateProtocol
import com.github.j5ik2o.adceet.api.write.domain.{ AccountId, ThreadId }
import wvlet.airframe.ulid.ULID

import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scala.concurrent.{ ExecutionContext, Future }

final class CreateThreadUseCaseImpl(
    private val system: ActorSystem[_],
    private val threadAggregateRef: ActorRef[ThreadAggregateProtocol.CommandRequest],
    private val askTimeout: FiniteDuration = 120.seconds
) extends CreateThreadUseCase {

  override def execute(threadId: ThreadId, accountId: AccountId)(implicit ec: ExecutionContext): Future[ThreadId] = {
    implicit val to: Timeout          = askTimeout
    implicit val scheduler: Scheduler = system.scheduler
    threadAggregateRef
      .ask(replyTo => ThreadAggregateProtocol.CreateThread(ULID.newULID, threadId, accountId, replyTo)).flatMap {
        case ThreadAggregateProtocol.CreateThreadSucceeded(_, _, threadId) =>
          Future.successful(threadId)
        case ThreadAggregateProtocol.CreateThreadFailed(_, _, _, error) =>
          Future.failed(new CreateThreadException(error.message))
      }
  }

}
