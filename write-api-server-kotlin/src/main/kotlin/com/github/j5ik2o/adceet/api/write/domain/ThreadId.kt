package com.github.j5ik2o.adceet.api.write.domain

import arrow.core.Either
import wvlet.airframe.ulid.ULID

data class ThreadId(val value: ULID = ULID.newULID()) : ValueObject {
  companion object {
    fun parseFromString(text: String): Either<Exception, ThreadId> {
      return try {
        Either.Right(ThreadId(ULID.fromString(text)))
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
