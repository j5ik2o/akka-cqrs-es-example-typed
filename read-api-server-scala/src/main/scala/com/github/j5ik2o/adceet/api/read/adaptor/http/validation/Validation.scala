/*
 * Copyright 2022 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.j5ik2o.adceet.api.read.adaptor.http.validation

import cats.data.ValidatedNel
import cats.implicits._

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
    AccountId
      .parseFromString(value).fold(
        { ex => ValidationError.ParseError(ex.toString).invalidNel }, {
          _.validNel
        }
      )
  }

  def validateThreadIdWithAccountId(
      threadIdString: String,
      accountIdString: String
  ): ValidatedNel[ValidationError, (ThreadId, AccountId)] = {
    (Validator.validateThreadId(threadIdString), Validator.validateAccountId(accountIdString)).mapN {
      case (threadId, accountId) =>
        (threadId, accountId)
    }
  }
}
