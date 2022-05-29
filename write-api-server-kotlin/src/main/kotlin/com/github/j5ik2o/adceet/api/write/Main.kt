package com.github.j5ik2o.adceet.api.write

import akka.actor.typed.ActorSystem
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.kodein.di.direct
import org.kodein.di.instance

enum class RunMode { SINGLE, MULTI }

class ExampleArgs(parser: ArgParser) {
  val runModeFor by parser.mapping(
    "--single" to RunMode.SINGLE,
    "--multi" to RunMode.MULTI,
    help = "what to run mode for"
  ).default { RunMode.MULTI }
}

fun main(args: Array<String>): Unit = mainBody {
  val parsedArgs = ArgParser(args).parseInto(::ExampleArgs)
  parsedArgs.run {
    val system: ActorSystem<MainActor.Companion.Command> = DISettings.toDI(this).direct.instance()
    system.whenTerminated.toCompletableFuture().get()
  }
}
