package com.github.j5ik2o.adceet.api.write.adaptor.aggregate

import akka.actor.BootstrapSetup
import akka.actor.setup.ActorSystemSetup
import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import akka.serialization.jackson.JacksonObjectMapperProviderSetup
import com.github.j5ik2o.adceet.api.write.KotlinModuleJacksonObjectMapperFactory
import com.github.j5ik2o.adceet.api.write.domain.AccountId
import com.github.j5ik2o.adceet.api.write.domain.Message
import com.github.j5ik2o.adceet.api.write.domain.MessageId
import com.github.j5ik2o.adceet.api.write.domain.ThreadId
import com.typesafe.config.Config
import org.junit.jupiter.api.Assertions
import wvlet.airframe.ulid.ULID

abstract class AbstractThreadAggregateTestBase {

    private lateinit var testKit: ActorTestKit

    protected fun testKit(): ActorTestKit = testKit

    open fun setUp(config: Config) {
        val setup = ActorSystemSetup.create(BootstrapSetup.create(config)).withSetup(
            JacksonObjectMapperProviderSetup(KotlinModuleJacksonObjectMapperFactory())
        )
        val system = ActorSystem.create(Behaviors.empty<Any>(), "test-system", setup)
        testKit = ActorTestKit.create(system)
    }

    fun tearDown() {
        testKit().shutdownTestKit()
    }

    fun clear() {
        ThreadPersist.dispose()
    }

    // true: akka-persistenceを完全に分離してテストします。
    // false: akka-persistenceのinmemory pluginでテストを行います。
    internal val inMemoryMode: Boolean = false

    abstract fun behavior(id: ThreadId, inMemoryMode: Boolean): Behavior<ThreadAggregateProtocol.CommandRequest>

    private fun actorRef(id: ThreadId, inMemoryMode: Boolean): ActorRef<ThreadAggregateProtocol.CommandRequest> {
        return testKit().spawn(behavior(id, inMemoryMode))
    }

    fun shouldCreateThread() {
        val id = ThreadId()
        val threadRef = actorRef(id, inMemoryMode)
        val accountId = AccountId()

        val createThreadReplyProbe = testKit().createTestProbe<ThreadAggregateProtocol.CreateThreadReply>()
        threadRef.tell(
            ThreadAggregateProtocol.CreateThread(
                ULID.newULID(),
                id,
                accountId,
                createThreadReplyProbe.ref()
            )
        )
        val createThreadReply =
            createThreadReplyProbe.expectMessageClass(ThreadAggregateProtocol.CreateThreadSucceeded::class.java)
        Assertions.assertEquals(id, createThreadReply.threadId)
    }

    fun shouldAddMember() {
        val id = ThreadId()
        val threadRef = actorRef(id, inMemoryMode)
        val accountId1 = AccountId()
        val accountId2 = AccountId()

        val createThreadReplyProbe = testKit().createTestProbe<ThreadAggregateProtocol.CreateThreadReply>()
        threadRef.tell(
            ThreadAggregateProtocol.CreateThread(
                ULID.newULID(),
                id,
                accountId1,
                createThreadReplyProbe.ref()
            )
        )
        val createThreadReply =
            createThreadReplyProbe.expectMessageClass(ThreadAggregateProtocol.CreateThreadSucceeded::class.java)
        Assertions.assertEquals(id, createThreadReply.threadId)

        val addMemberReplyProbe = testKit().createTestProbe<ThreadAggregateProtocol.AddMemberReply>()
        threadRef.tell(ThreadAggregateProtocol.AddMember(ULID.newULID(), id, accountId2, addMemberReplyProbe.ref()))
        val addMemberReply =
            addMemberReplyProbe.expectMessageClass(ThreadAggregateProtocol.AddMemberSucceeded::class.java)
        Assertions.assertEquals(id, addMemberReply.threadId)
    }

    fun shouldAddMessage() {
        val id = ThreadId()
        val threadRef = actorRef(id, inMemoryMode)
        val accountId1 = AccountId()
        val accountId2 = AccountId()
        val messageId = MessageId()
        val body = "ABC"

        val createThreadReplyProbe = testKit().createTestProbe<ThreadAggregateProtocol.CreateThreadReply>()
        threadRef.tell(
            ThreadAggregateProtocol.CreateThread(
                ULID.newULID(),
                id,
                accountId1,
                createThreadReplyProbe.ref()
            )
        )
        val createThreadReply =
            createThreadReplyProbe.expectMessageClass(ThreadAggregateProtocol.CreateThreadSucceeded::class.java)
        Assertions.assertEquals(id, createThreadReply.threadId)

        val addMemberReplyProbe = testKit().createTestProbe<ThreadAggregateProtocol.AddMemberReply>()
        threadRef.tell(ThreadAggregateProtocol.AddMember(ULID.newULID(), id, accountId2, addMemberReplyProbe.ref()))
        val addMemberReply =
            addMemberReplyProbe.expectMessageClass(ThreadAggregateProtocol.AddMemberSucceeded::class.java)
        Assertions.assertEquals(id, addMemberReply.threadId)

        val addMessageReplyProbe = testKit().createTestProbe<ThreadAggregateProtocol.AddMessageReply>()
        threadRef.tell(
            ThreadAggregateProtocol.AddMessage(
                ULID.newULID(),
                id,
                Message(
                    messageId,
                    id,
                    accountId2,
                    body
                ),
                addMessageReplyProbe.ref()
            )
        )
        val addMessageReply =
            addMessageReplyProbe.expectMessageClass(ThreadAggregateProtocol.AddMessageSucceeded::class.java)
        Assertions.assertEquals(id, addMessageReply.threadId)
    }
}
