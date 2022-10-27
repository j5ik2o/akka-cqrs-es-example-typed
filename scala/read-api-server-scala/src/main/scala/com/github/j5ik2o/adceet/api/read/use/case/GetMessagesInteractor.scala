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
package com.github.j5ik2o.adceet.api.read.use.`case`
import com.github.j5ik2o.adceet.api.read.adaptor.http.validation.ThreadId
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

final class GetMessagesInteractor(override val profile: JdbcProfile,
                            db: JdbcProfile#Backend#Database) extends GetMessagesUseCase {
    import profile.api._
  override def execute(threadId: ThreadId): Future[Seq[MessageRecord]] = {
    val query = MessagesQuery.filter(_.threadId === threadId.asString).result
    db.run(query)
  }

}
