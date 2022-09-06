package com.github.j5ik2o.adceet.api.write.adaptor.http.validation

import cats.data.ValidatedNel
import cats.implicits.catsSyntaxValidatedId
import com.github.j5ik2o.adceet.api.write.domain.{AccountId, ThreadId}

sealed abstract class ValidationError(val msg: String) {}

object ValidationError {
  final case class ParseError(override val msg: String) extends ValidationError(msg)
}

object Validator {
  def validateThreadId(value: String): ValidatedNel[ValidationError, ThreadId] = {
    ThreadId
      .parseFromString(value).fold(
        { ex => ValidationError.ParseError(ex.toString).invalidNel }, {
          _.validNel
        }
      )
  }

  def validateAccountId(value: String): ValidatedNel[ValidationError, AccountId] = {
    return AccountId
      .parseFromString(value).fold(
        { ex => ValidationError.ParseError(ex.toString).invalidNel }, {
          _.validNel
        }
      )
  }
}
