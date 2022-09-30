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
package com.github.j5ik2o.adceet.api.write.use.`case`

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.util.Timeout
import com.github.j5ik2o.adceet.api.write.aggregate.ThreadAggregateProtocol
import com.github.j5ik2o.adceet.domain.{AccountId, ThreadId}
import wvlet.airframe.ulid.ULID

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

final class AddMemberInteractor(
    private val system: ActorSystem[_],
    private val threadAggregateRef: ActorRef[ThreadAggregateProtocol.CommandRequest],
    private val askTimeout: FiniteDuration = 30.seconds
) extends AddMemberUseCase {

  override def execute(threadId: ThreadId, accountId: AccountId)(implicit ec: ExecutionContext): Future[ThreadId] = {
    implicit val to: Timeout          = askTimeout
    implicit val scheduler: Scheduler = system.scheduler
    threadAggregateRef
      .ask(replyTo => ThreadAggregateProtocol.AddMember(ULID.newULID, threadId, accountId, replyTo)).flatMap {
        case ThreadAggregateProtocol.AddMemberSucceeded(_, _, threadId) =>
          Future.successful(threadId)
        case ThreadAggregateProtocol.AddMemberFailed(_, _, _, error) =>
          Future.failed(new CreateThreadException(error.message))
      }
  }

}
