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
package com.github.j5ik2o.adceet.api.read

sealed trait Environment extends EnumEntry

object Environments extends Enum[Environment] {
  case object Development extends Environment
  case object Production extends Environment

  override def values: IndexedSeq[Environment] = findValues

  implicit val weekDaysRead: scopt.Read[Environment] =
    scopt.Read.reads(Environments.withNameInsensitive)

}

final case class Args(environment: Environment = Environments.Production)

object Main extends App {

}
