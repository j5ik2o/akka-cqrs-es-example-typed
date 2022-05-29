package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers
import akka.http.javadsl.server.Route
import ch.megard.akka.http.cors.javadsl.CorsDirectives
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController
import com.github.swagger.akka.javadsl.Converter
import com.github.swagger.akka.javadsl.SwaggerGenerator
import io.swagger.v3.oas.models.info.Info

class SwaggerDocService(private val host: String, private val port: Int) : AllDirectives() {

  private val generator = object : SwaggerGenerator {
    override fun converter(): Converter {
      return Converter(this)
    }

    override fun apiClasses(): MutableSet<Class<*>> {
      return mutableSetOf(
        ThreadController::class.java
      )
    }

    override fun apiDocsPath(): String {
      return "api-docs"
    }

    override fun host(): String {
      return "$host:$port"
    }

    override fun info(): Info {
      val info = Info()
      info.title = "thread service api"
      info.version = "v1"
      return info
    }

    override fun schemes(): MutableList<String> {
      return mutableListOf("http")
    }
  }

  fun toRoute(): Route {
    val route: Route = concat(
      path("swagger") {
        getFromResource("swagger/index.html")
      },
      getFromResourceDirectory("swagger"),
      path(
        PathMatchers.segment(generator.apiDocsPath()).slash("swagger.json")
      ) { get { complete(generator.generateSwaggerJson()) } },
    )

    val settings: CorsSettings = CorsSettings.defaultSettings()
    return CorsDirectives.cors(settings) { route }
  }
}
