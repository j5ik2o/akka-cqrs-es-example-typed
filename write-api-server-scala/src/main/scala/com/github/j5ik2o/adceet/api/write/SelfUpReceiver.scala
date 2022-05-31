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
