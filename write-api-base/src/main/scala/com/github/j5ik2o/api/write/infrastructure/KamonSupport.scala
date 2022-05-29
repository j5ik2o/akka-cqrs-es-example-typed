package com.github.j5ik2o.api.write.infrastructure

import kamon.Kamon
import kamon.trace.Span
import kamon.util.CallingThreadExecutionContext

import scala.concurrent.Future
import scala.util.Failure

object KamonSupport {

  def span[T](operationName: String)(f: => Future[T]): Future[T] = {
    span(operationName, null)(f)
  }

  def span[T](operationName: String, component: String)(f: => Future[T]): Future[T] = {
    val span = Kamon
      .spanBuilder(operationName)
      .kind(Span.Kind.Internal)
      .tagMetrics(Span.TagKeys.Component, component)
      .start()

    Kamon
      .runWithContextEntry(Span.Key, span)(f).andThen {
        case Failure(t) =>
          span
            .fail(t)
            .finish()
        case _ =>
          span.finish()
      }(CallingThreadExecutionContext)
  }

}
