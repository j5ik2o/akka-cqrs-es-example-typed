package com.github.j5ik2o.adceet.api.write

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.cluster.typed.SelfUp
import akka.http.javadsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.pattern.Patterns
import com.github.j5ik2o.adceet.api.write.adaptor.http.Routes
import com.github.j5ik2o.adceet.api.write.adaptor.http.controller.ThreadController
import kamon.Kamon
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.on
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Supplier

class MainActor constructor(override val di: DI, ctx: ActorContext<Command>) :
  AbstractBehavior<MainActor.Companion.Command>(ctx), DIAware {
  companion object {
    sealed interface Command

    internal object MeUp : Command

    fun create(di: DI, args: ExampleArgs): Behavior<Command> {
      return Behaviors.setup { ctx ->
        if (args.runModeFor == RunMode.PRODUCTION)
          Kamon.init()

        val config = ctx.system.settings().config()
        val host = config.getString("http.host")
        val port = config.getInt("http.port")
        val terminationHardDeadLine = config.getDuration("management.http.termination-hard-deadline")
        val loadBalancerDetachWaitDuration =
          config.getDuration("management.http.load-balancer-detach-wait-duration")

        // アプリケーションの起動
        val appServer = startHttpServer(di, ctx, host, port, terminationHardDeadLine)
        appServer.thenApply {
          // ヘルスチェックの起動
          startHealthCheckServer(ctx.system, loadBalancerDetachWaitDuration)
        }.toCompletableFuture().get() // FIXME: タイムアウトを設定する

        // クラスターブートストラップ
        val clusterBootstrap: ClusterBootstrap by di.on(ctx).instance()
        clusterBootstrap.start()

        // クラスターにジョインしたことを確認するための子アクターを生成する
        val selfUpRefFactory: (ActorRef<Command>) -> ActorRef<SelfUp> by di.on(ctx).factory()
        selfUpRefFactory(ctx.self)

        Behaviors.receive(Command::class.java)
          .onMessage(MeUp::class.java) {
            MainActor(di, ctx)
          }
          .onSignal(PostStop::class.java) {
            if (args.runModeFor == RunMode.PRODUCTION)
              Kamon.stop()
            Behaviors.same()
          }.build()
      }
    }

    private val logger: Logger = LoggerFactory.getLogger("main")

    private fun startHttpServer(
      di: DI,
      ctx: ActorContext<Command>,
      host: String,
      port: Int,
      terminationHardDeadLine: Duration
    ): CompletionStage<Done> {
      val threadController: ThreadController by di.on(ctx).instance()

      val http = Http.get(ctx.system).newServerAt(host, port)
        .bind(Routes(threadController).toRoute())
        .thenApply {
          it.addToCoordinatedShutdown(terminationHardDeadLine, ctx.system)
        }.whenComplete { serverBinding, throwable ->
          serverBinding?.let {
            val address = it.localAddress()
            logger.info("server bound to http://${address.hostString}:${address.port}")
          } ?: throwable?.let {
            logger.error("Failed to bind endpoint, terminating system: $it")
            ctx.system.terminate()
          }
        }
      return http.thenApply { Done.getInstance() }
    }

    private fun startHealthCheckServer(
      system: ActorSystem<Void>,
      loadBalancerDetachWaitDuration: Duration
    ): CompletionStage<Done> {
      val typeName = "akka-management"
      val coordinatedShutdown = CoordinatedShutdown.get(system)
      val akkaManagement = AkkaManagement.get(system)
      return akkaManagement.start().thenApply {
        logger.info("[$typeName] サーバ(${akkaManagement.settings().httpHostname}:${akkaManagement.settings().httpPort})を起動しました")
        coordinatedShutdown.addTask(
          CoordinatedShutdown.PhaseBeforeServiceUnbind(), "management-terminate",
          Supplier {
            logger.info("[$typeName] 終了処理のため $akkaManagement を終了します")
            akkaManagement.stop().thenApply {
              logger.info("[$typeName] $akkaManagement を terminate しました")
              logger.info("[$typeName] LoadBalancer からの切り離しを待つため、$loadBalancerDetachWaitDuration 待機します")
              Patterns.after(loadBalancerDetachWaitDuration, system) {
                logger.info("[$typeName] LoadBalancer からの切り離し待ち待機を完了しました")
                CompletableFuture.completedStage(Done.getInstance())
              }
              Done.getInstance()
            }
          }
        )
        Done.getInstance()
      }
    }
  }

  override fun createReceive(): Receive<Command> {
    val builder = newReceiveBuilder()
    return builder.build()
  }
}
