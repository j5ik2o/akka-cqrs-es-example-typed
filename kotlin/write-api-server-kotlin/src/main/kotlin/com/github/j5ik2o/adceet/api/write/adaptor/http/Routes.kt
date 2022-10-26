package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.Route
import akka.http.javadsl.server.directives.RouteAdapter
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController
import scala.collection.immutable.Set

class Routes(private val threadController: ThreadController) : AllDirectives() {

  fun toRoute(): Route {
    return handleExceptions(ExceptionHandlers.defaultHandler) {
      handleRejections(RejectionHandlers.defaultHandler) {
        val swaggerDocServiceRoute: Route = RouteAdapter(SwaggerDocService("127.0.0.1", 8081, Set.Set1(ThreadController::class.java)).toRoute())
        concat(hello(), swaggerDocServiceRoute, threadController.toRoute())
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
