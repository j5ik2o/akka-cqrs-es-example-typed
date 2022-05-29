package com.github.j5ik2o.adceet.api.write.domain

import wvlet.airframe.ulid.ULID

final case class MessageId(val value: ULID = ULID.newULID) extends ValueObject

object MessageId {
  def parseFromString(text: String): Either[Exception, MessageId] = {
    try {
      Right(MessageId(ULID.fromString(text)))
    } catch {
      case ex: NumberFormatException    => Left(ex)
      case ex: IllegalArgumentException => Left(ex)
      case ex: Throwable                => throw ex
    }
  }
}
