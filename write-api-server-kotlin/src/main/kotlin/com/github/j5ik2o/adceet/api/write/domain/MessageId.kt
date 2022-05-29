package com.github.j5ik2o.adceet.api.write.domain

import wvlet.airframe.ulid.ULID

data class MessageId(val value: ULID = ULID.newULID())
