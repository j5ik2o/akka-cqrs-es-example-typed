package com.github.j5ik2o.adceet.api.write

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JacksonObjectMappers {
  val default: ObjectMapper
    get() {
      val jom = JsonMapper.builder().enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build()
      return jom.registerKotlinModule()
    }
}
