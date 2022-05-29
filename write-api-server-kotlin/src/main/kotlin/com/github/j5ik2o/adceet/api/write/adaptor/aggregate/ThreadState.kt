package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import com.github.j5ik2o.adceet.api.write.domain.Thread
import com.github.j5ik2o.adceet.api.write.domain.ThreadCreated
import com.github.j5ik2o.adceet.api.write.domain.ThreadEvent
import com.github.j5ik2o.adceet.api.write.domain.ThreadId

sealed interface ThreadState {

  fun applyEvent(threadEvent: ThreadEvent): ThreadState {
    if (this is Empty && threadEvent is ThreadCreated) {
      return create(threadEvent)
    } else if (this is Just) {
      return update(threadEvent)
    }
    throw IllegalStateException()
  }

  companion object {
    data class Empty(val id: ThreadId) : ThreadState {
      fun create(event: ThreadCreated): Just =
        Just(Thread.applyEvent(event))
    }

    data class Just(val thread: Thread) : ThreadState {
      fun update(threadEvent: ThreadEvent) = Just(thread.updateEvent(threadEvent))
    }
  }
}
