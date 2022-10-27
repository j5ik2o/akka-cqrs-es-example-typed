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
import com.github.j5ik2o.adceet.api.read.adaptor.http.json.MessageJson
import com.github.j5ik2o.adceet.api.read.adaptor.http.validation.{ValidationRejection, Validator}
import com.github.j5ik2o.adceet.api.read.use.`case`.GetMessagesUseCase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

final class MessageController(private val getMessagesInteractor: GetMessagesUseCase) extends FailFastCirceSupport{
  def toRoute: Route = {
    concat(getMessages)
  }

  def getMessages: Route = {
    path("messages") {
      get {
        extractExecutionContext { implicit ec =>
          parameter("thread_id") { threadIdString =>
            Validator
              .validateThreadId(threadIdString).fold(
              { errors =>
                reject(ValidationRejection(errors))
              },
              { threadId =>
                val result = getMessagesInteractor.execute(threadId)
                onSuccess(result) { messages =>
                  complete(
                    StatusCodes.OK,
                    messages.map(message => MessageJson(message.id, message.threadId, message.accountId, message.text, message.createdAt)))
                }
              }
            )
          }
        }
      }
    }

  }
}
