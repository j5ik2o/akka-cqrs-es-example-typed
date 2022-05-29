package com.github.j5ik2o.adceet.api.write.use.case

import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import java.util.concurrent.CompletionStage

interface AddMessageUseCase {
  fun execute(message: Message): CompletionStage<ThreadId>
}
