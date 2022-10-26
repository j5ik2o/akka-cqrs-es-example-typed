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
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.j5ik2o.adceet.domain.ThreadId

object ThreadAggregates {
  final val name = "threads"

  def create(nameF: ThreadId => String)(
      childBehaviorF: ThreadId => Behavior[ThreadAggregateProtocol.CommandRequest]
  ): Behavior[ThreadAggregateProtocol.CommandRequest] = {
    Behaviors.setup { ctx =>
      def getOrCreateRef(threadId: ThreadId): ActorRef[ThreadAggregateProtocol.CommandRequest] = {
        ctx.child(nameF(threadId)) match {
          case Some(ref) =>
            ref.unsafeUpcast
          case None =>
            ctx.spawn(childBehaviorF(threadId), nameF(threadId))
        }
      }
      Behaviors.receiveMessage { msg =>
        getOrCreateRef(msg.threadId) ! msg
        Behaviors.same
      }
    }
  }
}
