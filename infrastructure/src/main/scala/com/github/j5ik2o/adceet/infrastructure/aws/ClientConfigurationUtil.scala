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
package com.github.j5ik2o.adceet.infrastructure.aws

import com.amazonaws.ClientConfiguration
import com.amazonaws.retry.RetryMode
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration._

object ClientConfigurationUtil {
  def createFromConfig(config: Config): ClientConfiguration = {
    val maxConnections =
      config
        .getOrElse[Int]("max-connections", ClientConfiguration.DEFAULT_MAX_CONNECTIONS)
    val connectionTTL = config
      .getOrElse[FiniteDuration](
        "connection-ttL",
        ClientConfiguration.DEFAULT_CONNECTION_TTL.milliseconds
      )
    val connectionTimeout = config.getOrElse[FiniteDuration](
      "connection-timeout",
      ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT.milliseconds
    )
    val connectionMaxIdleTime = config.getOrElse[FiniteDuration](
      "connection-max-idle-time",
      ClientConfiguration.DEFAULT_CONNECTION_MAX_IDLE_MILLIS.milliseconds
    )
    val clientExecutionTimeout = config.getOrElse[FiniteDuration](
      "client-execution-timeout",
      ClientConfiguration.DEFAULT_CLIENT_EXECUTION_TIMEOUT.milliseconds
    )
    val requestTimeout = config.getOrElse[FiniteDuration](
      "request-timeout",
      ClientConfiguration.DEFAULT_REQUEST_TIMEOUT.milliseconds
    )
    val maxErrorRetry = config.getAs[Int]("max-error-retry")
    val retryMode = config.getAs[String]("retry-mode")

    val cc = new ClientConfiguration()
    // * public ClientConfiguration withProtocol(Protocol protocol) {
    cc.setMaxConnections(maxConnections)
    // * public ClientConfiguration withUserAgent(String userAgent) {
    // * public ClientConfiguration withUserAgentPrefix(String prefix) {
    // * public ClientConfiguration withUserAgentSuffix(String suffix) {
    // * public ClientConfiguration withLocalAddress(InetAddress localAddress) {
    // * public ClientConfiguration withProxyProtocol(Protocol proxyProtocol) {
    // * public ClientConfiguration withProxyHost(String proxyHost) {
    // * public ClientConfiguration withProxyPort(int proxyPort) {
    // * public ClientConfiguration withDisableSocketProxy(boolean disableSocketProxy) {
    // * public ClientConfiguration withProxyUsername(String proxyUsername) {
    // * public ClientConfiguration withProxyPassword(String proxyPassword) {
    // * public ClientConfiguration withProxyDomain(String proxyDomain) {
    // * public ClientConfiguration withProxyWorkstation(String proxyWorkstation) {
    // * public ClientConfiguration withNonProxyHosts(String nonProxyHosts) {
    // * public ClientConfiguration withProxyAuthenticationMethods(List<ProxyAuthenticationMethod> proxyAuthenticationMethods) {
    // * public ClientConfiguration withRetryPolicy(RetryPolicy retryPolicy) {
    maxErrorRetry.foreach { n => cc.setMaxErrorRetry(n) }
    retryMode.foreach { s =>
      val e = RetryMode.fromName(s)
      cc.setRetryMode(e)
    }
    // * public ClientConfiguration withSocketTimeout(int socketTimeout) {
    if (connectionTimeout != Duration.Zero)
      cc.setConnectionTimeout(
        connectionTimeout.toMillis.toInt
      )
    if (requestTimeout != Duration.Zero)
      cc.setRequestTimeout(requestTimeout.toMillis.toInt)
    // * public ClientConfiguration withClientExecutionTimeout(int clientExecutionTimeout) {
    // * public ClientConfiguration withReaper(boolean use) {
    // * public ClientConfiguration withThrottledRetries(boolean use) {
    // * public ClientConfiguration withMaxConsecutiveRetriesBeforeThrottling(int maxConsecutiveRetriesBeforeThrottling) {
    // * public ClientConfiguration withGzip(boolean use) {
    // * public ClientConfiguration withSocketBufferSizeHints(int socketSendBufferSizeHint,
    // * public ClientConfiguration withSignerOverride(final String value) {
    // * public ClientConfiguration withPreemptiveBasicProxyAuth(boolean preemptiveBasicProxyAuth) {
    if (connectionTTL != Duration.Zero)
      cc.setConnectionTTL(connectionTTL.toMillis)

    if (connectionMaxIdleTime != Duration.Zero)
      cc.setConnectionMaxIdleMillis(connectionMaxIdleTime.toMillis)
    // * public ClientConfiguration withValidateAfterInactivityMillis(int validateAfterInactivityMillis) {
    // * public ClientConfiguration withTcpKeepAlive(final boolean use) {
    // * public ClientConfiguration withDnsResolver(final DnsResolver resolver) {
    // * public ClientConfiguration withCacheResponseMetadata(final boolean shouldCache) {
    // * public ClientConfiguration withResponseMetadataCacheSize(int responseMetadataCacheSize) {
    // * public ClientConfiguration withSecureRandom(SecureRandom secureRandom) {
    // * public ClientConfiguration withUseExpectContinue(boolean useExpectContinue) {
    // * public ClientConfiguration withHeader(String name, String value) {
    // * public ClientConfiguration withDisableHostPrefixInjection(boolean disableHostPrefixInjection) {
    // * public ClientConfiguration withTlsKeyManagersProvider(TlsKeyManagersProvider tlsKeyManagersProvider) {
    if (clientExecutionTimeout != Duration.Zero)
      cc.setClientExecutionTimeout(
        clientExecutionTimeout.toMillis.toInt
      )

    cc
  }
}
