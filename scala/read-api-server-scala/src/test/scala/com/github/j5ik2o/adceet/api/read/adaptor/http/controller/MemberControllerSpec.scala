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
package com.github.j5ik2o.adceet.api.read.adaptor.http.controller

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.j5ik2o.adceet.api.read.adaptor.http.json.{MemberJson, MessageJson}
import com.github.j5ik2o.adceet.api.read.adaptor.http.validation.ThreadId
import com.github.j5ik2o.adceet.api.read.use.`case`.{GetMembersUseCase, GetMessagesUseCase}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import wvlet.airframe.ulid.ULID

import java.time.Instant
import scala.concurrent.Future

class MemberControllerSpec
    extends AnyFreeSpec
    with Matchers
    with ScalatestRouteTest
    with FailFastCirceSupport
    with MockitoSugar
    with ArgumentMatchersSugar
    with HttpTestSupport {

  "MemberController" - {
    "getMembers" in {
      val getMembersInteractor = mock[GetMembersUseCase]
      val memberController     = new MemberController(getMembersInteractor)

      val accountId = ULID.newULIDString
      val threadId  = ThreadId()

      when(getMembersInteractor.execute(threadId)).thenAnswer { threadId: ThreadId =>
        val id = ULID.newULIDString
        Future.successful(
          Seq(getMembersInteractor.MemberRecord(threadId.asString, accountId, Instant.now))
        )
      }

      Get(s"/members?thread_id=${threadId.asString}") ~> memberController.toRoute ~> check {
        val response = responseAs[Seq[MemberJson]]
        response.size shouldBe 1
        response.head.thread_id shouldBe threadId.asString
      }
    }
  }

}
