package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.Directives.complete
import akka.http.javadsl.server.ExceptionHandler
import com.github.j5ik2o.adceet.api.write.use.case.ThreadException

object ExceptionHandlers {

  val defaultHandler: ExceptionHandler
    get() {
      val builder = ExceptionHandler.newBuilder()
      builder.match(ThreadException::class.java) { ex ->
        complete(StatusCodes.BAD_REQUEST, ex.message)
      }
      return builder.build()
    }
}
