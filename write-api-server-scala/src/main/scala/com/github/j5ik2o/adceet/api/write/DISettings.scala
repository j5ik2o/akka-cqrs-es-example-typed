package com.github.j5ik2o.adceet.api.write
import akka.actor.{ ClassicActorSystemProvider, ExtendedActorSystem }
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ ActorRef, ActorSystem, Scheduler }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.SelfUp
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.github.j5ik2o.adceet.api.write.aggregate._
import com.github.j5ik2o.adceet.api.write.use.`case`.{
  AddMemberUseCase,
  AddMemberUseCaseImpl,
  AddMessageUseCase,
  AddMessageUseCaseImpl,
  CreateThreadUseCase,
  CreateThreadUseCaseImpl
}
import com.typesafe.config.{ Config, ConfigFactory }
import wvlet.airframe._

object DISettings {

  val di: DesignWithContext[_] = newDesign
    .bind[Config].toInstance(ConfigFactory.load())
    .bind[ActorSystem[MainActor.Command]].toProvider[Session, Config] { (session, config) =>
      ActorSystem(new MainActor(session).create, "adceet", config)
    }
    .bind[Scheduler].toProvider[ActorSystem[MainActor.Command]] { system =>
      system.scheduler
    }

  def mainActor(ctx: ActorContext[MainActor.Command]): DesignWithContext[_] = {
    newDesign
      .bind[ClusterBootstrap].toInstance(
        ClusterBootstrap(ctx.system)
      )
      .bind[ActorRef[SelfUp]].toInstance(
        ctx.spawn(SelfUpReceiver.create(ctx.self), "self-up")
      )
      .bind[ClusterSharding].toInstance(ClusterSharding(ctx.system))
      .bind[ActorRef[ThreadAggregateProtocol.CommandRequest]].toProvider[ClusterSharding] { clusterSharding =>
        ShardedThreadAggregate.initClusterSharding(
          clusterSharding,
          ThreadAggregates.create { _.asString } { id =>
            ThreadAggregate.create(id) { (id, ref) =>
              ThreadPersist.persistBehavior(
                id,
                ref
              )
            }
          }
        )
        ctx.spawn(ShardedThreadAggregate.ofProxy(clusterSharding), "sharded-thread")
      }
      .bind[CreateThreadUseCase].toProvider[ActorRef[ThreadAggregateProtocol.CommandRequest]] { actorRef =>
        new CreateThreadUseCaseImpl(ctx.system, actorRef)
      }
      .bind[AddMemberUseCase].toProvider[ActorRef[ThreadAggregateProtocol.CommandRequest]] { actorRef =>
        new AddMemberUseCaseImpl(ctx.system, actorRef)
      }
      .bind[AddMessageUseCase].toProvider[ActorRef[ThreadAggregateProtocol.CommandRequest]] { actorRef =>
        new AddMessageUseCaseImpl(ctx.system, actorRef)
      }
  }
}
