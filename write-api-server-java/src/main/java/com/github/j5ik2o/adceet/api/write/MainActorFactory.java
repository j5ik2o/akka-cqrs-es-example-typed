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
package com.github.j5ik2o.adceet.api.write;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import akka.http.javadsl.Http;
import com.github.j5ik2o.adceet.api.write.adaptor.http.Routes;
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wvlet.log.io.StopWatch;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public final class MainActorFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(MainActorFactory.class);

  static CompletionStage<Done> startHttpServer(
      Injector injector,
      ActorSystem<Void> system,
      String host,
      int port,
      Duration terminationHardDeadLine) {
    var threadController = injector.getInstance(Key.get(new TypeLiteral<ThreadController>() {}));
    var http =
        Http.get(system)
            .newServerAt(host, port)
            .bind(new Routes(threadController).toRoute())
            .thenApply(it -> it.addToCoordinatedShutdown(terminationHardDeadLine, system))
            .whenComplete(
                (serverBinding, throwable) -> {
                  if (serverBinding != null) {
                    var address = serverBinding.localAddress();
                    LOGGER.info(
                        "server bound to http://%s:%d"
                            .formatted(address.getHostString(), address.getPort()));
                  }
                  if (throwable != null) {
                    LOGGER.error("Failed to bind endpoint, terminating system", throwable);
                    system.terminate();
                  }
                });
    return http.thenApply(ignore -> Done.getInstance());
  }

  public static Behavior<MainProtocol.Command> create(Injector injector, StopWatch stopWatch) {
    return Behaviors.setup(
        ctx -> {
          LOGGER.info("[{}] create", stopWatch.reportElapsedTime());
          var cluster = Cluster.get(ctx.getSystem());
          var selfMember = cluster.selfMember();
          LOGGER.info(
              "[{}] Specified role(s) = {}",
              stopWatch.reportElapsedTime(),
              selfMember.getRoles().stream().reduce("", "%s, %s"::formatted));

          LOGGER.info("selfMember.roles = {}", selfMember.getRoles());
          var roleNames = RoleNames.from(selfMember);

          var config = ctx.getSystem().settings().config();
          var adceetConfig = config.getConfig("adceet");
          var host = adceetConfig.getString("http.host");
          var port = adceetConfig.getInt("http.port");
          var terminationHardDeadLine =
              adceetConfig.getDuration("management.http.termination-hard-deadline");
          var loadBalancerDetachWaitDuration =
              adceetConfig.getDuration("management.http.load-balancer-detach-wait-duration");

          var childInjector = injector.createChildInjector(new MainActorModule(ctx, roleNames));
          // childInjector.getInstance()

          return new MainActor(ctx);
        });
  }
}
