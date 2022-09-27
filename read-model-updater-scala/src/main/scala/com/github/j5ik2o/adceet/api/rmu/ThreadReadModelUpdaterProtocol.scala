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
package com.github.j5ik2o.adceet.api.rmu

import akka.actor.typed.ActorRef

object ThreadReadModelUpdaterProtocol {
  sealed trait Command

  trait CommandReply

  case class StopWithReply(replyTo: ActorRef[Stopped]) extends Command

  case object Stop extends Command

  case class Stopped() extends CommandReply

  case class StartWithReply(streamArn: String, replyTo: ActorRef[Started]) extends Command

  case class Start(streamArn: String) extends Command

  case class Started() extends CommandReply

  private[rmu] case class WrappedStartedResult(replyTo: ActorRef[Started], msg: Started) extends Command

  private[rmu] case class WrappedStoppedResult(replyTo: ActorRef[Stopped], msg: Stopped) extends Command
}
