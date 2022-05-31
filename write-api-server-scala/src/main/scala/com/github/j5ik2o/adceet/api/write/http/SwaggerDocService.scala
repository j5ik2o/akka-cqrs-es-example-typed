package com.github.j5ik2o.adceet.api.write.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.github.j5ik2o.adceet.api.write.http.controller.ThreadController
import com.github.swagger.akka.{ model, SwaggerGenerator }

class SwaggerDocService(private val hostName: String, private val port: Int) {

  val generator: SwaggerGenerator = new SwaggerGenerator() {

    override def apiClasses: Set[Class[_]] = Set(
      classOf[ThreadController]
    )

    override def schemes: List[String] = List("http")

    override def host: String = s"$hostName:$port"

    override def apiDocsPath: String = "api-docs"

    override def info: model.Info = {
      val info = model.Info()
      info.setTitle("thread service api")
      info.setVersion("v1")
      info
    }
  }

  def toRoute: Route = {
    val route: Route = concat(
      path("swagger") {
        getFromResource("swagger/index.html")
      },
      getFromResourceDirectory("swagger"),
      path(
        generator.apiDocsPath / "swagger.json"
      ) { get { complete(generator.generateSwaggerJson) } }
    )

    val settings: CorsSettings = CorsSettings.defaultSettings
    CorsDirectives.cors(settings) { route }
  }
}
