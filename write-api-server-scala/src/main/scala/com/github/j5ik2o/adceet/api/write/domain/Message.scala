package com.github.j5ik2o.adceet.api.write.domain

final case class Message(id: MessageId, threadId: ThreadId, senderId: AccountId, body: String) extends ValueObject
