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
package com.github.j5ik2o.adceet.api.write.adaptor.http.validation;

import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.error.ParseError;
import com.github.j5ik2o.adceet.api.write.adaptor.http.validation.error.ValidationError;
import com.github.j5ik2o.adceet.api.write.domain.AccountId;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import fj.data.NonEmptyList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Validation;

public class Validator {

  public static Validation<NonEmptyList<ValidationError>, ThreadId> validateThreadId(String value) {
    try {
      return Validation.valid(ThreadId.parseFrom(value));
    } catch (Exception ex) {
      return Validation.invalid(NonEmptyList.nel(new ParseError(ex.getMessage())));
    }
  }

  public static Validation<NonEmptyList<ValidationError>, AccountId> validateAccountId(
      String value) {
    try {
      return Validation.valid(AccountId.parseFrom(value));
    } catch (Exception ex) {
      return Validation.invalid(NonEmptyList.nel(new ParseError(ex.getMessage())));
    }
  }

  public static Validation<NonEmptyList<ValidationError>, Tuple2<ThreadId, AccountId>>
      validateThreadIdWithAccountId(String threadId, String accountId) {
    var sequence =
        Validation.combine(validateThreadId(threadId), validateAccountId(accountId)).ap(Tuple::of);
    return sequence.mapError(errors -> errors.reduce(NonEmptyList::append));
  }
}
