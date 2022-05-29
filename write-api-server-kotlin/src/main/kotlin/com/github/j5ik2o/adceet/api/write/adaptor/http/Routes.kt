package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.Route
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController

class Routes(private val threadController: ThreadController) : AllDirectives() {

  fun toRoute(): Route {
    return handleExceptions(ExceptionHandlers.defaultHandler) {
      handleRejections(RejectionHandlers.defaultHandler) {
        concat(hello(), SwaggerDocService("127.0.0.1", 8081).toRoute(), threadController.toRoute())
      }
    }
  }

  private fun hello(): Route {
    return concat(
      path("hello") {
        get {
          complete(
            "Say hello to akka-http"
          )
        }
      }
    )
  }
}
