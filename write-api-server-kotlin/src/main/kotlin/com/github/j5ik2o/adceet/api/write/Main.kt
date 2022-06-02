package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.ActorSystem
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.kodein.di.direct
import org.kodein.di.instance

enum class RunMode { DEVELOPMENT, PRODUCTION }

class ExampleArgs(parser: ArgParser) {

  val runModeFor by parser.storing("-e", "--env", help = "what to run mode for") {
    RunMode.valueOf(uppercase())
  }

}

fun main(args: Array<String>): Unit = mainBody {
  val parsedArgs = ArgParser(args).parseInto(::ExampleArgs)
  parsedArgs.run {
    val system: ActorSystem<MainActor.Companion.Command> = DISettings.toDI(this).direct.instance()
    system.whenTerminated.toCompletableFuture().get()
  }
}
