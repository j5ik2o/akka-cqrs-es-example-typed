package com.github.j5ik2o.adceet.api.write.http.validation

sealed abstract class ValidationError(val msg: String) {}

object ValidationError {
  final case class ParseError(override val msg: String) extends ValidationError(msg)
}
