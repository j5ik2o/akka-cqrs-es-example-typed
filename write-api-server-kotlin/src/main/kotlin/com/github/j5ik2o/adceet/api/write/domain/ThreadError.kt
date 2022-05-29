package com.github.j5ik2o.adceet.api.write.domain

sealed interface ThreadError {
  val message: String
  companion object {
    data class ThreadCountOverError(override val message: String) : ThreadError
    data class ExistsMemberError(val accountId: AccountId) : ThreadError {
      override val message: String = "This account($accountId) is already a member."
    }

    data class NotMemberError(val accountId: AccountId) : ThreadError {
      override val message = "This account($accountId) is not a member."
    }
    data class MessageSizeOverError(override val message: String) : ThreadError
  }
}
