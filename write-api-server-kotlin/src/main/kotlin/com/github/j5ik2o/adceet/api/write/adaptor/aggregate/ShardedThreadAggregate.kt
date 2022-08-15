package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.javadsl.ClusterSharding
import akka.cluster.sharding.typed.javadsl.Entity
import akka.cluster.sharding.typed.javadsl.EntityContext
import akka.cluster.sharding.typed.javadsl.EntityTypeKey
import java.time.Duration

object ShardedThreadAggregate {

  private val TypeKey: EntityTypeKey<ThreadAggregateProtocol.CommandRequest> =
    EntityTypeKey.create(ThreadAggregateProtocol.CommandRequest::class.java, "Thread")

  private fun entityBehavior(
    childBehavior: Behavior<ThreadAggregateProtocol.CommandRequest>,
    receiveTimeout: Duration? = null
  ): (EntityContext<ThreadAggregateProtocol.CommandRequest>) -> Behavior<ThreadAggregateProtocol.CommandRequest> = { entityContext ->
    Behaviors.setup { ctx ->
      val childRef = ctx.spawn(childBehavior, ThreadAggregates.name)
      receiveTimeout?.let { duration ->
        ctx.setReceiveTimeout(duration, ThreadAggregateProtocol.Idle)
      }
      Behaviors.receiveMessage { cmd ->
        when (cmd) {
          is ThreadAggregateProtocol.Idle -> {
            ctx.log.debug("entityBehavior#receive ThreadAggregateProtocol.Idle")
            entityContext.shard.tell(ClusterSharding.Passivate(ctx.self))
            Behaviors.same()
          }
          is ThreadAggregateProtocol.Stop -> {
            ctx.log.debug("entityBehavior#receive ThreadAggregateProtocol.Stop")
            Behaviors.stopped()
          }
          else -> {
            childRef.tell(cmd)
            Behaviors.same()
          }
        }
      }
    }
  }

  fun initClusterSharding(
    clusterSharding: ClusterSharding,
    childBehavior: Behavior<ThreadAggregateProtocol.CommandRequest>?,
    receiveTimeout: Duration? = null
  ): ActorRef<ShardingEnvelope<ThreadAggregateProtocol.CommandRequest>> {
    val entity = Entity.of(
      TypeKey,
      entityBehavior(
        childBehavior ?: Behaviors.empty(),
        receiveTimeout
      )
    ).withStopMessage(ThreadAggregateProtocol.Stop)
    return clusterSharding.init(entity)
  }

  fun ofProxy(clusterSharding: ClusterSharding): Behavior<ThreadAggregateProtocol.CommandRequest> {
    return Behaviors.receiveMessage { msg ->
      val entityRef = clusterSharding.entityRefFor(TypeKey, msg.threadId.asString())
      entityRef.tell(msg)
      Behaviors.same()
    }
  }
}
