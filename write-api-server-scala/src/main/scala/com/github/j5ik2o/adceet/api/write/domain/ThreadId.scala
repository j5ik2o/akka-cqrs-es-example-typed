package com.github.j5ik2o.adceet.api.write.domain

import wvlet.airframe.ulid.ULID

final case class ThreadId(value: ULID = ULID.newULID) extends ValueObject {
  def asString: String = value.toString()
}

object ThreadId {

  def parseFromString(text: String): Either[Exception, ThreadId] = {
    try {
      Right(ThreadId(ULID.fromString(text)))
    } catch {
      case ex: NumberFormatException    => Left(ex)
      case ex: IllegalArgumentException => Left(ex)
      case ex: Throwable                => throw ex
    }
  }

}
