package com.github.j5ik2o.adceet.api.write.domain

sealed trait ThreadError {
  def message: String
}

object ThreadError {

  final case class ThreadCountOverError(message: String) extends ThreadError
  final case class ExistsMemberError(accountId: AccountId) extends ThreadError {
    override def message: String = s"This account($accountId) is already a member."
  }
  final case class NotMemberError(accountId: AccountId) extends ThreadError {
    override def message: String = s"This account($accountId) is not a member."
  }
  final case class MessageSizeOverError(message: String) extends ThreadError

}
