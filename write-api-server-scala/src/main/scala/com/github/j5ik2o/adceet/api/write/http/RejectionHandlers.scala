package com.github.j5ik2o.adceet.api.write.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RejectionHandler
import com.github.j5ik2o.adceet.api.write.http.json.ErrorsResponseJson
import com.github.j5ik2o.adceet.api.write.http.validation.ValidationRejection
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

object RejectionHandlers extends FailFastCirceSupport {

  val defaultHandler: RejectionHandler = {
    RejectionHandler
      .newBuilder()
      .handle { case ValidationRejection(errorMessages) =>
        val responseJson = ErrorsResponseJson(errorMessages.map(_.msg))
        complete(StatusCodes.BadRequest, responseJson)
      }.result()
  }

}
