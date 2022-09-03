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
package com.github.j5ik2o.adceet.api.write.adaptor.aggregate;

import akka.actor.BootstrapSetup;
import akka.actor.setup.ActorSystemSetup;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.serialization.jackson.JacksonObjectMapperFactory;
import akka.serialization.jackson.JacksonObjectMapperProviderSetup;
import com.github.j5ik2o.adceet.api.write.adaptor.aggregate.protocol.ThreadAggregateProtocol;
import com.github.j5ik2o.adceet.api.write.domain.AccountId;
import com.github.j5ik2o.adceet.api.write.domain.Message;
import com.github.j5ik2o.adceet.api.write.domain.MessageId;
import com.github.j5ik2o.adceet.api.write.domain.ThreadId;
import com.typesafe.config.Config;
import org.junit.jupiter.api.Assertions;
import wvlet.airframe.ulid.ULID;

abstract class AbstractThreadAggregateTestBase {
  private ActorTestKit testKit;

  protected ActorTestKit testKit() {
    return testKit;
  }

  void setUp(Config config) {
    var setup =
        ActorSystemSetup.create(BootstrapSetup.create(config))
            .withSetup(new JacksonObjectMapperProviderSetup(new JacksonObjectMapperFactory()));
    var system = ActorSystem.create(Behaviors.empty(), "test-system", setup);
    testKit = ActorTestKit.create(system);
  }

  void tearDown() {
    testKit().shutdownTestKit();
  }

  void clear() {
    ThreadPersistFactory.dispose();
  }

  private boolean inMemoryMode = false;

  abstract Behavior<ThreadAggregateProtocol.CommandRequest> behavior(
      ThreadId id, boolean inMemoryMode);

  private ActorRef<ThreadAggregateProtocol.CommandRequest> actorRef(
      ThreadId id, boolean inMemoryMode) {
    return testKit().spawn(behavior(id, inMemoryMode));
  }

  void shouldCreateThread() {
    var id = new ThreadId();
    var threadRef = actorRef(id, inMemoryMode);
    var accountId = new AccountId();

    var createThreadReplyProbe =
        testKit().createTestProbe(ThreadAggregateProtocol.CreateThreadReply.class);
    threadRef.tell(
        new ThreadAggregateProtocol.CreateThread(
            ULID.newULID(), id, accountId, createThreadReplyProbe.ref()));
    var createThreadReply =
        createThreadReplyProbe.expectMessageClass(
            ThreadAggregateProtocol.CreateThreadSucceeded.class);
    Assertions.assertEquals(id, createThreadReply.threadId());
  }

  void shouldAddMember() {
    var id = new ThreadId();
    var threadRef = actorRef(id, inMemoryMode);
    var accountId1 = new AccountId();
    var accountId2 = new AccountId();

    var createThreadReplyProbe =
        testKit().createTestProbe(ThreadAggregateProtocol.CreateThreadReply.class);
    threadRef.tell(
        new ThreadAggregateProtocol.CreateThread(
            ULID.newULID(), id, accountId1, createThreadReplyProbe.ref()));
    var createThreadReply =
        createThreadReplyProbe.expectMessageClass(
            ThreadAggregateProtocol.CreateThreadSucceeded.class);
    Assertions.assertEquals(id, createThreadReply.threadId());

    var addMemberReplyProbe =
        testKit().createTestProbe(ThreadAggregateProtocol.AddMemberReply.class);
    threadRef.tell(
        new ThreadAggregateProtocol.AddMember(
            ULID.newULID(), id, accountId2, addMemberReplyProbe.ref()));
    var addMemberReply =
        addMemberReplyProbe.expectMessageClass(ThreadAggregateProtocol.AddMemberSucceeded.class);
    Assertions.assertEquals(id, addMemberReply.threadId());
  }

  void shouldAddMessage() {
    var id = new ThreadId();
    var threadRef = actorRef(id, inMemoryMode);
    var accountId1 = new AccountId();
    var accountId2 = new AccountId();
    var messageId = new MessageId();
    var body = "ABC";

    var createThreadReplyProbe =
        testKit().createTestProbe(ThreadAggregateProtocol.CreateThreadReply.class);
    threadRef.tell(
        new ThreadAggregateProtocol.CreateThread(
            ULID.newULID(), id, accountId1, createThreadReplyProbe.ref()));
    var createThreadReply =
        createThreadReplyProbe.expectMessageClass(
            ThreadAggregateProtocol.CreateThreadSucceeded.class);
    Assertions.assertEquals(id, createThreadReply.threadId());

    var addMemberReplyProbe =
        testKit().createTestProbe(ThreadAggregateProtocol.AddMemberReply.class);
    threadRef.tell(
        new ThreadAggregateProtocol.AddMember(
            ULID.newULID(), id, accountId2, addMemberReplyProbe.ref()));
    var addMemberReply =
        addMemberReplyProbe.expectMessageClass(ThreadAggregateProtocol.AddMemberSucceeded.class);
    Assertions.assertEquals(id, addMemberReply.threadId());

    var addMessageReplyProbe =
        testKit().createTestProbe(ThreadAggregateProtocol.AddMessageReply.class);
    threadRef.tell(
        new ThreadAggregateProtocol.AddMessage(
            ULID.newULID(),
            id,
            new Message(messageId, id, accountId2, body),
            addMessageReplyProbe.ref()));
    var addMessageReply =
        addMessageReplyProbe.expectMessageClass(ThreadAggregateProtocol.AddMessageSucceeded.class);
    Assertions.assertEquals(id, addMessageReply.threadId());
  }
}
