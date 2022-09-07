/*
 * Copyright 2022 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
