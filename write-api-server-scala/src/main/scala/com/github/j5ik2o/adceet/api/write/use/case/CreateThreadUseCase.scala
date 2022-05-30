package com.github.j5ik2o.adceet.api.write.use.`case`

import com.github.j5ik2o.adceet.api.write.domain.{ AccountId, ThreadId }

import scala.concurrent.{ ExecutionContext, Future }

trait CreateThreadUseCase {
  def execute(threadId: ThreadId, accountId: AccountId)(implicit ec: ExecutionContext): Future[ThreadId]
}
