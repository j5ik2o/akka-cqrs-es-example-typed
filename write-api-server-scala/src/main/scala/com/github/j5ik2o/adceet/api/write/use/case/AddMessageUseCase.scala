package com.github.j5ik2o.adceet.api.write.use.`case`

import com.github.j5ik2o.adceet.api.write.domain.{ Message, ThreadId }

import scala.concurrent.{ ExecutionContext, Future }

trait AddMessageUseCase {

  def execute(message: Message)(implicit ec: ExecutionContext): Future[ThreadId]

}
