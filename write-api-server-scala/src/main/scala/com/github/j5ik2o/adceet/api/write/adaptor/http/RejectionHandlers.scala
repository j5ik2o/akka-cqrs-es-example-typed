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
package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import com.github.j5ik2o.adceet.api.write.adaptor.http.json.ErrorsResponseJson
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationRejection
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

object RejectionHandlers extends FailFastCirceSupport {

  val defaultHandler: RejectionHandler = {
    RejectionHandler
      .newBuilder()
      .handle { case ValidationRejection(errorMessages) =>
        val responseJson = ErrorsResponseJson(errorMessages.map(_.msg).toList)
        complete(StatusCodes.BadRequest, responseJson)
      }.result()
  }

}
