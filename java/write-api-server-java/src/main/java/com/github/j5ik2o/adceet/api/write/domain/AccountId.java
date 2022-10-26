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
package com.github.j5ik2o.adceet.api.write.domain;

import wvlet.airframe.ulid.ULID;

public final class AccountId {

  public static AccountId parseFrom(String text) {
    return new AccountId(ULID.fromString(text));
  }

  public AccountId(ULID value) {
    this.value = value;
  }

  public AccountId() {
    this(ULID.newULID());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccountId accountId = (AccountId) o;

    return value.equals(accountId.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  public String asString() {
    return value.toString();
  }

  @Override
  public String toString() {
    return "AccountId{" + "value=" + value + '}';
  }

  private final ULID value;
}
