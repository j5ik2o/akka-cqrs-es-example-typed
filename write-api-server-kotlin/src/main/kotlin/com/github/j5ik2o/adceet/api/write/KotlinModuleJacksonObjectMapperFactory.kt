package com.github.j5ik2o.adceet.api.write

import akka.serialization.jackson.JacksonObjectMapperFactory
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper

class KotlinModuleJacksonObjectMapperFactory : JacksonObjectMapperFactory() {
  override fun newObjectMapper(bindingName: String, jsonFactory: JsonFactory): ObjectMapper {
    return JacksonObjectMappers.default
  }
}
