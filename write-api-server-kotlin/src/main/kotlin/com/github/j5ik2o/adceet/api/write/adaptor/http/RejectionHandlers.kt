package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.Directives.complete
import akka.http.javadsl.server.RejectionHandler
import com.github.j5ik2o.adceet.api.write.JacksonObjectMappers
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.ValidationRejection
import com.github.j5ik2o.adceet.api.write.adaptor.json.ErrorsResponseJson

object RejectionHandlers {

  val defaultHandler: RejectionHandler
    get() {
      val builder = RejectionHandler.newBuilder()
      builder.handle(ValidationRejection::class.java) { rejection ->
        val responseJson = ErrorsResponseJson(rejection.errorMessages.map { it.msg }.toList())
        complete(StatusCodes.BAD_REQUEST, responseJson, Jackson.marshaller(JacksonObjectMappers.default))
      }
      return builder.build()
    }
}
