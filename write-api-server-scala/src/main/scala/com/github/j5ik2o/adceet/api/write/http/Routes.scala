package com.github.j5ik2o.adceet.api.write.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.j5ik2o.adceet.api.write.http.controller.ThreadController

class Routes(private val threadController: ThreadController) {

  def toRoute: Route = {
    handleExceptions(ExceptionHandlers.defaultHandler) {
      handleRejections(RejectionHandlers.defaultHandler) {
        concat(hello, new SwaggerDocService("127.0.0.1", 8081).toRoute, threadController.toRoute)
      }
    }
  }

  private def hello: Route = {
    concat(
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
