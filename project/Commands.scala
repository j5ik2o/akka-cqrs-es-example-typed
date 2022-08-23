package adceet_sbtecr

import sbt.Logger

import scala.language.postfixOps
import scala.sys.process._

private[adceet_sbtecr] object Commands {

  def exec(cmd: String)(implicit logger: Logger): Int = synchronized {
    logger.debug(s"Executing (2.12): ${cmd}")
    val p      = Process(cmd).run()
    val result = p.exitValue()
    logger.debug(s"exitValue = $result")
    result
  }

}
