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
