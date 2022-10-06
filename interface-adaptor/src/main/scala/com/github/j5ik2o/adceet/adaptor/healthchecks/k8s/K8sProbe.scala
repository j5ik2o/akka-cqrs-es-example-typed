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
package com.github.j5ik2o.adceet.adaptor.healthchecks.k8s

import akka.http.scaladsl.server.Route
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.HealthCheck
import com.github.j5ik2o.adceet.adaptor.healthchecks.core.route.HealthCheckRoutes

import scala.concurrent.ExecutionContext

sealed abstract class K8sProbe protected (val checks: List[HealthCheck], val path: String, val ec: ExecutionContext) {
  require(checks.nonEmpty, "checks must not be empty.")

  def toRoute: Route = HealthCheckRoutes.health(path, checks)(ec)
}

case class LivenessProbe protected (
    override val checks: List[HealthCheck],
    override val path: String,
    override val ec: ExecutionContext
) extends K8sProbe(checks, path, ec)

case class ReadinessProbe protected (
    override val checks: List[HealthCheck],
    override val path: String,
    override val ec: ExecutionContext
) extends K8sProbe(checks, path, ec)
