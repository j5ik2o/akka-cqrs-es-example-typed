package com.github.j5ik2o.adceet.api.write.domain

import arrow.core.Either
import wvlet.airframe.ulid.ULID
import java.lang.NumberFormatException

data class AccountId(val value: ULID = ULID.newULID()) : ValueObject {
  companion object {
    fun parseFromString(text: String): Either<Exception, AccountId> {
      return try {
        Either.Right(AccountId(ULID.fromString(text)))
      } catch (ex: Exception) {
        when (ex) {
          is NumberFormatException, is IllegalArgumentException ->
            Either.Left(ex)
          else -> throw ex
        }
      }
    }
  }

  fun asString() = value.toString()
}
