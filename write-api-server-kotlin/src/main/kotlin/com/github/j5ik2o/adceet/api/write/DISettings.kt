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
import com.github.j5ik2o.adceet.api.write.use.case.AddMemberInteractor
import com.github.j5ik2o.adceet.api.write.use.case.AddMemberUseCase
import com.github.j5ik2o.adceet.api.write.use.case.AddMessageInteractor
import com.github.j5ik2o.adceet.api.write.use.case.AddMessageUseCase
import com.github.j5ik2o.adceet.api.write.use.case.CreateThreadInteractor
import com.github.j5ik2o.adceet.api.write.use.case.CreateThreadUseCase
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
import wvlet.log.io.StopWatch

object DISettings {
  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  data class CtxWithArgs(val ctx: ActorContext<MainActor.Companion.Command>, val roleNames: List<RoleName>)

  fun toDI(args: ExampleArgs, stopWatch: StopWatch): DI = DI {
    val ctxScope = WeakContextScope.of<CtxWithArgs>()
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
          MainActor.create(di, args, stopWatch), "kamon-example-default",
          setup
        )
      }
    }
    bindSingleton<Scheduler> {
      instance<ActorSystem<MainActor.Companion.Command>>().scheduler()
    }
    bind<ClusterBootstrap> {
      scoped(ctxScope).singleton {
        ClusterBootstrap.get(context.ctx.system)
      }
    }
    bind<ActorRef<SelfUp>> {
      scoped(ctxScope).factory { replyTo: ActorRef<MainActor.Companion.Command> ->
        context.ctx.spawn(SelfUpReceiver.create(replyTo), "self-up")
      }
    }
    bind<ClusterSharding> {
      scoped(ctxScope).singleton {
        ClusterSharding.get(context.ctx.system)
      }
    }
    bind<ActorRef<ThreadAggregateProtocol.CommandRequest>> {
      scoped(ctxScope).singleton {
        val behavior = if (context.roleNames.contains(RoleName.BACKEND)) {
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
        } else {
          null
        }
        ShardedThreadAggregate.initClusterSharding(instance(), behavior)
        context.ctx.spawn(ShardedThreadAggregate.ofProxy(instance()), "sharded-thread")
      }
    }
    bind<CreateThreadUseCase> {
      scoped(ctxScope).singleton {
        CreateThreadInteractor(context.ctx.system, instance())
      }
    }
    bind<AddMemberUseCase> {
      scoped(ctxScope).singleton {
        AddMemberInteractor(context.ctx.system, instance())
      }
    }
    bind<AddMessageUseCase> {
      scoped(ctxScope).singleton {
        AddMessageInteractor(context.ctx.system, instance())
      }
    }
    bind<ThreadController> {
      scoped(ctxScope).singleton {
        ThreadController(instance(), instance(), instance())
      }
    }
  }
}
