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
package com.github.j5ik2o.adceet.api.read.adaptor.http.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.j5ik2o.adceet.api.read.adaptor.http.json.ThreadJson
import com.github.j5ik2o.adceet.api.read.use.`case`.GetThreadsUseCase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

final class ThreadController(private val getThreadsInteractor: GetThreadsUseCase) extends FailFastCirceSupport {
  def getThreads(): Route = {
    path("threads") {
      parameter("owner_id") { ownerId =>
        get {
          extractExecutionContext { implicit ec =>
            val result = getThreadsInteractor.execute(ownerId)
            onSuccess(result) { threads =>
              complete(StatusCodes.OK, threads.map(thread => ThreadJson(thread.id, thread.ownerId, thread.createdAt)))
            }
          }
        }
      }
    }
  }
}
