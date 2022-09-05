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

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public final class MainActor extends AbstractBehavior<MainProtocol.Command> {

    public MainActor(ActorContext<MainProtocol.Command> context) {
        super(context);
    }

    @Override
    public Receive<MainProtocol.Command> createReceive() {
        var builder = newReceiveBuilder();
        return builder.build();
    }
}
