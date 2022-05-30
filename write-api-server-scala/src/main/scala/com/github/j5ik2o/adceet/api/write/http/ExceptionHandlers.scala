package com.github.j5ik2o.adceet.api.write.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import com.github.j5ik2o.adceet.api.write.use.`case`.ThreadException
import akka.http.scaladsl.server.Directives._

object ExceptionHandlers {

  val defaultHandler: ExceptionHandler = ExceptionHandler { case ex: ThreadException =>
    complete(StatusCodes.BadRequest, ex.message)
  }

}
