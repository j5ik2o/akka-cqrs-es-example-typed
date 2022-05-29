package com.github.j5ik2o.adceet.api.write.use.case

import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import java.util.concurrent.CompletionStage

interface CreateThreadUseCase {
  fun execute(threadId: ThreadId, accountId: AccountId): CompletionStage<ThreadId>
}
