package com.github.j5ik2o.adceet.api.write;/*
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

import akka.actor.BootstrapSetup;
import akka.actor.setup.ActorSystemSetup;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Scheduler;
import akka.serialization.jackson.JacksonObjectMapperFactory;
import akka.serialization.jackson.JacksonObjectMapperProviderSetup;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import wvlet.log.io.StopWatch;

public class MainModule extends AbstractModule {
    @Override protected void configure() {}

    @Provides
    private Config config() {
        return ConfigFactory.load();
    }

    @Provides
    private Behavior<MainProtocol.Command> mainBehavior() {
        var stopWatch = new StopWatch();
        return MainActorFactory.create(stopWatch);
    }

    @Provides
    private ActorSystem<MainProtocol.Command> system(Config config, Behavior<MainProtocol.Command> mainBehavior) {
        var setup = ActorSystemSetup.create(BootstrapSetup.create(config)).withSetup(
                new JacksonObjectMapperProviderSetup(new JacksonObjectMapperFactory())
        );
        return ActorSystem.create(
                mainBehavior,
                "kamon-example-default",
                setup
        );
    }

    @Provides
    private Scheduler scheduler(ActorSystem<MainProtocol.Command> system) {
        return system.scheduler();
    }
}
