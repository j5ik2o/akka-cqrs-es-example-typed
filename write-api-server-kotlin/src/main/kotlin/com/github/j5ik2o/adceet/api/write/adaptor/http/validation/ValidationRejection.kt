package com.github.j5ik2o.adceet.api.write.adaptor.http.validation

import akka.http.javadsl.server.CustomRejection
import arrow.core.NonEmptyList

data class ValidationRejection(val errorMessages: NonEmptyList<ValidationError>) : CustomRejection
