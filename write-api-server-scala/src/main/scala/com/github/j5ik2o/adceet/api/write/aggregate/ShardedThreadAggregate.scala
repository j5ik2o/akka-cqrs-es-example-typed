package com.github.j5ik2o.adceet.api.write.aggregate

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity, EntityContext, EntityTypeKey }

import scala.concurrent.duration.FiniteDuration

object ShardedThreadAggregate {

  private val TypeKey: EntityTypeKey[ThreadAggregateProtocol.CommandRequest] = EntityTypeKey("Thread")

  private def entityBehavior(
      childBehavior: Behavior[ThreadAggregateProtocol.CommandRequest],
      receiveTimeout: Option[FiniteDuration] = None
  ): EntityContext[ThreadAggregateProtocol.CommandRequest] => Behavior[ThreadAggregateProtocol.CommandRequest] = {
    entityContext =>
      Behaviors.setup { ctx =>
        val childRef = ctx.spawn(childBehavior, ThreadAggregates.name)
        receiveTimeout.foreach { duration =>
          ctx.setReceiveTimeout(duration, ThreadAggregateProtocol.Idle)
        }
        Behaviors.receiveMessage {
          case ThreadAggregateProtocol.Idle =>
            ctx.log.debug("entityBehavior#receive ThreadAggregateProtocol.Idle")
            entityContext.shard ! ClusterSharding.Passivate(ctx.self)
            Behaviors.same
          case ThreadAggregateProtocol.Stop =>
            ctx.log.debug("entityBehavior#receive ThreadAggregateProtocol.Stop")
            Behaviors.stopped
          case cmd =>
            childRef ! cmd
            Behaviors.same
        }
      }
  }

  def initClusterSharding(
      clusterSharding: ClusterSharding,
      childBehavior: Behavior[ThreadAggregateProtocol.CommandRequest],
      receiveTimeout: Option[FiniteDuration] = None
  ): ActorRef[ShardingEnvelope[ThreadAggregateProtocol.CommandRequest]] = {
    val entity = Entity(TypeKey)(
      entityBehavior(
        childBehavior,
        receiveTimeout
      )
    ).withStopMessage(ThreadAggregateProtocol.Stop)
    clusterSharding.init(entity)
  }

  def ofProxy(clusterSharding: ClusterSharding): Behavior[ThreadAggregateProtocol.CommandRequest] = {
    Behaviors.receiveMessage { msg =>
      val entityRef = clusterSharding.entityRefFor(TypeKey, msg.threadId.asString)
      entityRef ! msg
      Behaviors.same
    }
  }

}
