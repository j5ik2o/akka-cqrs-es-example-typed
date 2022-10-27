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
package com.github.j5ik2o.adceet.api.read

import akka.actor.typed.{ActorSystem, Scheduler}
import com.github.j5ik2o.adceet.api.read.use.`case`.{GetMembersInteractor, GetMembersUseCase, GetMessagesInteractor, GetMessagesUseCase, GetThreadsInteractor, GetThreadsUseCase}
import com.typesafe.config.{Config, ConfigFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import wvlet.airframe.{DesignWithContext, Session, newDesign}
import wvlet.log.io.StopWatch

object DISettings {
  def di(args: Args, stopWatch: StopWatch): DesignWithContext[_] = newDesign
    .bind[Config].toInstance(ConfigFactory.load())
    .bind[ActorSystem[MainActor.Command]].toProvider[Session, Config] { (session, config) =>
      ActorSystem(new MainActor(session, stopWatch).create(args), "adceet", config)
    }
    .bind[Scheduler].toProvider[ActorSystem[MainActor.Command]] { system =>
      system.scheduler
    }.bind[DatabaseConfig[JdbcProfile]].toProvider[Config] { config =>
      DatabaseConfig.forConfig[JdbcProfile]("slick", config)
    }
    .bind[GetThreadsUseCase].toProvider[DatabaseConfig[JdbcProfile]] { databaseConfig =>
      new GetThreadsInteractor(databaseConfig.profile, databaseConfig.db)
    }
    .bind[GetMembersUseCase].toProvider[DatabaseConfig[JdbcProfile]] { databaseConfig =>
      new GetMembersInteractor(databaseConfig.profile, databaseConfig.db)
    }
    .bind[GetMessagesUseCase].toProvider[DatabaseConfig[JdbcProfile]] { databaseConfig =>
    new GetMessagesInteractor(databaseConfig.profile, databaseConfig.db)
  }
}
