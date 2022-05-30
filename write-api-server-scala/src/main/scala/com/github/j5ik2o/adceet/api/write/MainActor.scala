package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object MainActor {
  sealed trait Command
  case object MeUp extends Command

  def create(): Behavior[Command] = {
    Behaviors.same
  }
}
