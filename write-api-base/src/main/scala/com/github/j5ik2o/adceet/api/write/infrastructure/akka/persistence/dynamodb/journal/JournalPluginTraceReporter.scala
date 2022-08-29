package com.github.j5ik2o.adceet.api.write.infrastructure.akka.persistence.dynamodb.journal

import com.github.j5ik2o.adceet.api.write.infrastructure.KamonSupport
import com.github.j5ik2o.akka.persistence.dynamodb.config.PluginConfig
import com.github.j5ik2o.akka.persistence.dynamodb.model.Context
import com.github.j5ik2o.akka.persistence.dynamodb.trace.TraceReporter

import scala.concurrent.Future

class JournalPluginTraceReporter(
    pluginConfig: PluginConfig
) extends TraceReporter {
  override def traceJournalAsyncWriteMessages[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalAsyncWriteMessages")(f)

  override def traceJournalAsyncDeleteMessagesTo[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalAsyncDeleteMessagesTo")(f)

  override def traceJournalAsyncReplayMessages[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalAsyncReplayMessages")(f)

  override def traceJournalAsyncReadHighestSequenceNr[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalAsyncReadHighestSequenceNr")(f)

  override def traceJournalAsyncUpdateEvent[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalAsyncUpdateEvent")(f)

  override def traceJournalSerializeJournal[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalSerializeJournals")(f)

  override def traceJournalDeserializeJournal[T](context: Context)(f: => Future[T]): Future[T] =
    KamonSupport.span("journalDeserializeJournal")(f)

  override def traceSnapshotStoreLoadAsync[T](context: Context)(f: => Future[T]): Future[T] = f

  override def traceSnapshotStoreSaveAsync[T](context: Context)(f: => Future[T]): Future[T] = f

  override def traceSnapshotStoreDeleteAsync[T](context: Context)(f: => Future[T]): Future[T] = f

  override def traceSnapshotStoreDeleteWithCriteriaAsync[T](context: Context)(f: => Future[T]): Future[T] = f

  override def traceSnapshotStoreSerializeSnapshot[T](context: Context)(f: => Future[T]): Future[T] = f

  override def traceSnapshotStoreDeserializeSnapshot[T](context: Context)(f: => Future[T]): Future[T] = f
}
