package com.github.j5ik2o.adceet.api.write.use.`case`

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ ActorRef, ActorSystem, Scheduler }
import akka.util.Timeout
import com.github.j5ik2o.adceet.api.write.aggregate.ThreadAggregateProtocol
import com.github.j5ik2o.adceet.api.write.domain.{ Message, ThreadId }
import wvlet.airframe.ulid.ULID

import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scala.concurrent.{ ExecutionContext, Future }

final class AddMessageInteractor(
    private val system: ActorSystem[_],
    private val threadAggregateRef: ActorRef[ThreadAggregateProtocol.CommandRequest],
    private val askTimeout: FiniteDuration = 30.seconds
) extends AddMessageUseCase {

  override def execute(message: Message)(implicit ec: ExecutionContext): Future[ThreadId] = {
    implicit val to: Timeout          = askTimeout
    implicit val scheduler: Scheduler = system.scheduler
    threadAggregateRef
      .ask(replyTo => ThreadAggregateProtocol.AddMessage(ULID.newULID, message.threadId, message, replyTo)).flatMap {
        case ThreadAggregateProtocol.AddMessageSucceeded(_, _, threadId) =>
          Future.successful(threadId)
        case ThreadAggregateProtocol.AddMessageFailed(_, _, _, error) =>
          Future.failed(new CreateThreadException(error.message))
      }
  }

}
