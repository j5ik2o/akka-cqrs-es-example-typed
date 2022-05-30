package com.github.j5ik2o.adceet.api.write.http.validation

import akka.http.javadsl.server.CustomRejection
import cats.data.NonEmptyList

final case class ValidationRejection(errorMessages: NonEmptyList[ValidationError]) extends CustomRejection
