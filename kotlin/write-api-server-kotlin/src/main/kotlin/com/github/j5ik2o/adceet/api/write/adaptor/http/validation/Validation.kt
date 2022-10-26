package com.github.j5ik2o.adceet.api.write.adaptor.http.validation

import arrow.core.Nel
import arrow.core.ValidatedNel
import arrow.core.invalidNel
import arrow.core.sequence
import arrow.core.validNel
import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.ThreadId

sealed class ValidationError(open val msg: String) {
  data class ParseError(override val msg: String) : ValidationError(msg)
}

object Validator {

  fun validateThreadId(value: String): ValidatedNel<ValidationError, ThreadId> {
    return ThreadId.parseFromString(value).fold(
      { ex -> ValidationError.ParseError(ex.toString()).invalidNel() },
      {
        it.validNel()
      }
    )
  }

  fun validateAccountId(value: String): ValidatedNel<ValidationError, AccountId> {
    return AccountId.parseFromString(value).fold(
      { ex -> ValidationError.ParseError(ex.toString()).invalidNel() },
      {
        it.validNel()
      }
    )
  }

  fun validateThreadIdWithAccountId(
    threadIdString: String,
    accountIdString: String
  ): ValidatedNel<ValidationError, Pair<ThreadId, AccountId>> = listOf(
    Validator.validateThreadId(threadIdString),
    Validator.validateAccountId(accountIdString)
  ).sequence().map {
    // mapNないがようなのでこういうやり方しかできない…。
    Pair(it[0] as ThreadId, it[1] as AccountId)
  }
}

fun Nel<ValidationError>.toErrorMessages(): String {
  return map { it.msg }.joinToString(separator = ", ")
}
