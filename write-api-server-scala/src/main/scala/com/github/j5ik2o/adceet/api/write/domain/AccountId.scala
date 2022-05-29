package com.github.j5ik2o.adceet.api.write.domain

import wvlet.airframe.ulid.ULID

final case class AccountId(value: ULID = ULID.newULID) extends ValueObject

object AccountId {

  def parseFromString(text: String): Either[Exception, AccountId] = {
    try {
      Right(AccountId(ULID.fromString(text)))
    } catch {
      case ex: NumberFormatException    => Left(ex)
      case ex: IllegalArgumentException => Left(ex)
      case ex: Throwable                => throw ex
    }
  }

}
