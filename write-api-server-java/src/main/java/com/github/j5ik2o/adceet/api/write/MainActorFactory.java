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

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wvlet.log.io.StopWatch;

public final class MainActorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActorFactory.class);

    public static Behavior<MainProtocol.Command> create(StopWatch stopWatch) {
        return Behaviors.setup(ctx -> {
            LOGGER.info("[{}] create", stopWatch.reportElapsedTime());
            var cluster = Cluster.get(ctx.getSystem());
            var selfMember = cluster.selfMember();
            LOGGER.info("[{}] Specified role(s) = {}", stopWatch.reportElapsedTime(), selfMember.getRoles().stream().reduce("", "%s, %s"::formatted));



            return new MainActor(ctx);
        });
    }
}
