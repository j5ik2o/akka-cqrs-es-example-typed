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
import akka.cluster.typed.Cluster
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
import wvlet.log.io.StopWatch
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Supplier

class MainActor constructor(override val di: DI, ctx: ActorContext<Command>) :
    AbstractBehavior<MainActor.Companion.Command>(ctx), DIAware {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MainActor.javaClass)

        sealed interface Command

        internal object MeUp : Command

        fun create(di: DI, args: ExampleArgs, stopWatch: StopWatch): Behavior<Command> {
            return Behaviors.setup { ctx ->
                if (args.runModeFor == RunMode.PRODUCTION)
                    Kamon.init()

                logger.info("[${stopWatch.reportElapsedTime()}] create")
                val cluster = Cluster.get(ctx.system)
                val selfMember = cluster.selfMember()
                logger.info("[${stopWatch.reportElapsedTime()}] Specified role(s) = ${selfMember.roles.joinToString(", ")}")

                logger.info("selfMember.roles = ${selfMember.roles}")
                require(selfMember.roles.isNotEmpty()) { "akka.cluster.roles are empty" }

                val roleNames = (if (selfMember.hasRole(RoleName.FRONTEND.toString().lowercase(Locale.getDefault()))) {
                    listOf(RoleName.FRONTEND)
                } else {
                    listOf()
                }) + (if (selfMember.hasRole(RoleName.FRONTEND.toString().lowercase(Locale.getDefault()))) {
                    listOf(RoleName.FRONTEND)
                } else {
                    listOf()
                })
                logger.info("roleNames = $roleNames")


                val config = ctx.system.settings().config()
                val adceetConfig = config.getConfig("adceet")
                val host = adceetConfig.getString("http.host")
                val port = adceetConfig.getInt("http.port")
                val terminationHardDeadLine = adceetConfig.getDuration("management.http.termination-hard-deadline")
                val loadBalancerDetachWaitDuration =
                    adceetConfig.getDuration("management.http.load-balancer-detach-wait-duration")

                val ctxWithArgs = DISettings.CtxWithArgs(ctx, roleNames)

                // アプリケーションの起動
                val appServer = if (roleNames.contains(RoleName.FRONTEND)) {
                        startHttpServer(di, ctxWithArgs, host, port, terminationHardDeadLine)
                    } else {
                        CompletableFuture.completedFuture(Unit)
                    }
                appServer.thenApply {
                    // ヘルスチェックの起動
                    startHealthCheckServer(ctx.system, loadBalancerDetachWaitDuration)
                }.toCompletableFuture().get() // FIXME: タイムアウトを設定する

                // クラスターブートストラップ
                val clusterBootstrap: ClusterBootstrap by di.on(ctxWithArgs).instance()
                clusterBootstrap.start()

                // クラスターにジョインしたことを確認するための子アクターを生成する
                val selfUpRefFactory: (ActorRef<Command>) -> ActorRef<SelfUp> by di.on(ctxWithArgs).factory()
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

        private fun startHttpServer(
            di: DI,
            ctxWithArgs: DISettings.CtxWithArgs,
            host: String,
            port: Int,
            terminationHardDeadLine: Duration
        ): CompletionStage<Done> {
            val threadController: ThreadController by di.on(ctxWithArgs).instance()

            val http = Http.get(ctxWithArgs.ctx.system).newServerAt(host, port)
                .bind(Routes(threadController).toRoute())
                .thenApply {
                    it.addToCoordinatedShutdown(terminationHardDeadLine, ctxWithArgs.ctx.system)
                }.whenComplete { serverBinding, throwable ->
                    serverBinding?.let {
                        val address = it.localAddress()
                        logger.info("server bound to http://${address.hostString}:${address.port}")
                    } ?: throwable?.let {
                        logger.error("Failed to bind endpoint, terminating system: $it")
                        ctxWithArgs.ctx.system.terminate()
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
