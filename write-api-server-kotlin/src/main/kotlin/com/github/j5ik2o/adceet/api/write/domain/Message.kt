package com.github.j5ik2o.adceet.api.write.domain

data class Message(val id: MessageId, val threadId: ThreadId, val senderId: AccountId, val body: String) : ValueObject
