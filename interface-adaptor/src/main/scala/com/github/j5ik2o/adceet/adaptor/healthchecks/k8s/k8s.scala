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
package com.github.j5ik2o.adceet.adaptor.healthchecks

import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.HealthCheck

import scala.concurrent.{ ExecutionContext, Future }

package object k8s {
  private def configPathRoot = "k8s_probe"

  private def config(system: ActorSystem, subPath: String = "") = {
    val path =
      if (subPath.nonEmpty) configPathRoot + "." + subPath else configPathRoot
    system.settings.config.getConfig(path)
  }

  def livenessProbe(
      checks: HealthCheck*
  )(implicit
      system: ActorSystem,
      ec: ExecutionContext
  ): LivenessProbe = {
    LivenessProbe(checks.toList, config(system, "path").getString("liveness"), ec)
  }

  def readinessProbe(
      checks: HealthCheck*
  )(implicit
      system: ActorSystem,
      ec: ExecutionContext
  ): ReadinessProbe = {
    ReadinessProbe(checks.toList, config(system, "path").getString("readiness"), ec)
  }

  def bindAndHandleProbes(
      probe: K8sProbe,
      probes: K8sProbe*
  )(implicit
      system: ActorSystem
  ): Future[Http.ServerBinding] = {
    val routes = (probe +: probes).toList
      .map(_.toRoute)
      .reduce((r1: Route, r2: Route) => r1 ~ r2)
    Http(system).newServerAt(host, port).bind(routes)
  }

  def host(implicit system: ActorSystem): String = config(system).getString("host")

  def port(implicit system: ActorSystem): Int = config(system).getInt("port")

}
