package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb.snapshot

import com.github.j5ik2o.adceet.api.write.infrastructure.KamonSupport
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.model.Context
import com.github.j5ik2o.akka.persistence.dynamodb.trace.TraceReporter

import scala.concurrent.Future

class SnapshotPluginTraceReporter(
    pluginConfig: PluginConfig
) extends TraceReporter {

  override def traceSnapshotStoreLoadAsync[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("snapshotStoreLoadAsync")(f)

  override def traceSnapshotStoreSaveAsync[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("snapshotStoreSaveAsync")(f)

  override def traceSnapshotStoreDeleteAsync[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("snapshotStoreDeleteAsync")(f)

  override def traceSnapshotStoreDeleteWithCriteriaAsync[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("snapshotStoreDeleteWithCriteriaAsync")(f)

  override def traceSnapshotStoreSerializeSnapshot[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("snapshotStoreSerializeSnapshot")(f)

  override def traceSnapshotStoreDeserializeSnapshot[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("snapshotStoreDeserializeSnapshot")(f)
}
