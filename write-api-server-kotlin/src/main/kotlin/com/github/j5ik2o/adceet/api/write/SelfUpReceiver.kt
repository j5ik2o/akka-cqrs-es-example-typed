package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.typed.Cluster
import akka.cluster.typed.SelfUp
import akka.cluster.typed.Subscribe
import akka.cluster.typed.Unsubscribe

object SelfUpReceiver {

  fun create(replyTo: ActorRef<MainActor.Companion.Command>): Behavior<SelfUp> = Behaviors.setup { ctx ->
    ctx.log.info("${ctx.system.name()} started and ready to join cluster")
    Cluster.get(ctx.system).subscriptions().tell(Subscribe(ctx.self, SelfUp::class.java))
    Behaviors.receive(SelfUp::class.java)
      .onMessage(SelfUp::class.java) {
        ctx.log.info("${ctx.system.name()} joined cluster and is up")
        replyTo.tell(MainActor.Companion.MeUp)
        Behaviors.stopped()
      }
      .onSignal(PostStop::class.java) {
        Cluster.get(ctx.system).subscriptions().tell(Unsubscribe(ctx.self))
        Behaviors.same()
      }.build()
  }
}
