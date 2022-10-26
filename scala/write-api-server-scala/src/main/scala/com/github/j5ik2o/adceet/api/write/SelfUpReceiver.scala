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
package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.{ ActorRef, Behavior, PostStop }
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ Cluster, SelfUp, Subscribe, Unsubscribe }

object SelfUpReceiver {

  def create(replyTo: ActorRef[MainActor.Command]): Behavior[SelfUp] = Behaviors.setup { ctx =>
    ctx.log.info(s"${ctx.system.name} started and ready to join cluster")
    Cluster(ctx.system).subscriptions ! Subscribe(ctx.self, classOf[SelfUp])
    Behaviors
      .receiveMessage[SelfUp] { case SelfUp(_) =>
        ctx.log.info(s"${ctx.system.name} joined cluster and is up")
        replyTo ! MainActor.MeUp
        Behaviors.stopped
      }.receiveSignal { case (ctx, PostStop) =>
        Cluster(ctx.system).subscriptions ! Unsubscribe(ctx.self)
        Behaviors.same
      }
  }

}
