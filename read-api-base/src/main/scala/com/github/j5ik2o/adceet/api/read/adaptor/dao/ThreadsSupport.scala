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
package com.github.j5ik2o.adceet.api.read.adaptor.dao

import slick.lifted.ProvenShape

import java.time.Instant

trait ThreadsSupport extends SlickSupport {
  import profile.api._

  final case class ThreadRecord(id: String, createdAt: Instant)

  class Threads(tag: Tag) extends Table[ThreadRecord](tag, "threads") {
    def id: Rep[String]         = column[String]("id")
    def createdAt: Rep[Instant] = column[Instant]("created_at")
    def pk         = primaryKey("pk", (id))
    override def * : ProvenShape[ThreadRecord] = (id,createdAt) <> (ThreadRecord.tupled, ThreadRecord.unapply)
  }

  val ThreadsQuery = TableQuery[Threads]

}
