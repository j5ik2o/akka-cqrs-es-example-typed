/*
 * Copyright 2022 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.j5ik2o.adceet.api.write.adaptor.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.github.swagger.akka.{ model, SwaggerGenerator }

class SwaggerDocService(private val hostName: String, private val port: Int, _apiClasses: Set[Class[_]]) {

  val generator: SwaggerGenerator = new SwaggerGenerator() {
    override def apiClasses: Set[Class[_]] = _apiClasses

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
      ) {
        get {
          complete(generator.generateSwaggerJson)
        }
      }
    )

    val settings: CorsSettings = CorsSettings.defaultSettings
    CorsDirectives.cors(settings) {
      route
    }
  }

}
