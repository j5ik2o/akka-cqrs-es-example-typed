package com.github.j5ik2o.adceet.api.write

import akka.actor.BootstrapSetup
import akka.actor.setup.ActorSystemSetup
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Scheduler
import akka.actor.typed.javadsl.ActorContext
import akka.cluster.sharding.typed.javadsl.ClusterSharding
import akka.cluster.typed.SelfUp
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.serialization.jackson.JacksonObjectMapperProviderSetup
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ShardedThreadAggregate
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadAggregate
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadAggregateProtocol
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadAggregates
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.ThreadPersist
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController
import com.github.j5ik2o.adceet.api.write.use.case.AddMemberUseCase
import com.github.j5ik2o.adceet.api.write.use.case.AddMemberUseCaseImpl
import com.github.j5ik2o.adceet.api.write.use.case.AddMessageUseCase
import com.github.j5ik2o.adceet.api.write.use.case.AddMessageUseCaseImpl
import com.github.j5ik2o.adceet.api.write.use.case.CreateThreadUseCase
import com.github.j5ik2o.adceet.api.write.use.case.CreateThreadUseCaseImpl
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.bindings.WeakContextScope
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DISettings {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  fun toDI(args: ExampleArgs): DI = DI {
    val ctxScope = WeakContextScope.of<ActorContext<MainActor.Companion.Command>>()
    bindSingleton<Config> {
      val config = ConfigFactory.load()
      logger.info("config = {}", config.root().render(ConfigRenderOptions.concise()))
      config
    }
    bind<ActorSystem<MainActor.Companion.Command>> {
      singleton {
        val setup = ActorSystemSetup.create(BootstrapSetup.create(instance())).withSetup(
          JacksonObjectMapperProviderSetup(KotlinModuleJacksonObjectMapperFactory())
        )
        ActorSystem.create(
          MainActor.create(di, args), "kamon-example-default",
          setup
        )
      }
    }
    bindSingleton<Scheduler> {
      instance<ActorSystem<MainActor.Companion.Command>>().scheduler()
    }
    bind<ClusterBootstrap> {
      scoped(ctxScope).singleton {
        ClusterBootstrap.get(context.system)
      }
    }
    bind<ActorRef<SelfUp>> {
      scoped(ctxScope).factory { replyTo: ActorRef<MainActor.Companion.Command> ->
        context.spawn(SelfUpReceiver.create(replyTo), "self-up")
      }
    }
    bind<ClusterSharding> {
      scoped(ctxScope).singleton {
        ClusterSharding.get(context.system)
      }
    }
    bind<ActorRef<ThreadAggregateProtocol.CommandRequest>> {
      scoped(ctxScope).singleton {
        ShardedThreadAggregate.initClusterSharding(
          instance(),
          ThreadAggregates.create(
            { it.asString() },
            { id ->
              ThreadAggregate.create(id) { id, ref ->
                ThreadPersist.persistBehavior(
                  id,
                  ref
                )
              }
            }
          )
        )
        context.spawn(ShardedThreadAggregate.ofProxy(instance()), "sharded-thread")
      }
    }
    bind<CreateThreadUseCase> {
      scoped(ctxScope).singleton {
        CreateThreadUseCaseImpl(context.system, instance())
      }
    }
    bind<AddMemberUseCase> {
      scoped(ctxScope).singleton {
        AddMemberUseCaseImpl(context.system, instance())
      }
    }
    bind<AddMessageUseCase> {
      scoped(ctxScope).singleton {
        AddMessageUseCaseImpl(context.system, instance())
      }
    }
    bind<ThreadController> {
      scoped(ctxScope).singleton {
        ThreadController(instance(), instance(), instance())
      }
    }
  }
}
