package com.github.j5ik2o.adceet.api.write.http.validation

import akka.http.javadsl.server.CustomRejection

final case class ValidationRejection(errorMessages: List[ValidationError]) extends CustomRejection
