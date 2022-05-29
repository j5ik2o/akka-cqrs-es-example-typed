package com.github.j5ik2o.adceet.api.write.aggregate

import com.github.j5ik2o.adceet.api.write.domain.ThreadEvents.{ ThreadCreated, ThreadEvent }
import com.github.j5ik2o.adceet.api.write.domain.{ Thread, ThreadId }

sealed trait ThreadState {
  def applyEvent(threadEvent: ThreadEvent): ThreadState = {
    (this, threadEvent) match {
      case (ts: ThreadState.Empty, t: ThreadCreated) =>
        ts.create(t)
      case (ts: ThreadState.Just, t) =>
        ts.update(t)
    }
  }
}

object ThreadState {
  case class Empty(threadId: ThreadId) extends ThreadState {
    def create(event: ThreadCreated): Just =
      Just(Thread.applyEvent(event))
  }
  case class Just(thread: Thread) extends ThreadState {
    def update(threadEvent: ThreadEvent): Just = Just(thread.updateEvent(threadEvent))
  }
}
