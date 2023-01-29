import Dependencies._

lazy val root = (project in file("."))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    name := "adceet-root"
  ).aggregate(
    `test-base-scala`,
    `write-api-base`,
    `write-api-server-scala`,
    `write-api-server-kotlin`,
    `write-api-server-java`,
    `read-model-updater-base`,
    `read-model-updater-scala`,
    `domain-scala`,
    `interface-adaptor`,
    `infrastructure-scala`
  )

lazy val `infrastructure-scala` = (project in file("scala/infrastructure-scala"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-infrastructure",
    libraryDependencies ++= Seq(
      airframe.ulid,
      iheart.ficus,
      typesafeAkka.akkaActorTyped,
      awssdk.v1.dynamodb,
      awssdk.v1.cloudwatch,
      cats.core
    )
  )

val circeVersion = "0.14.1"

lazy val `interface-adaptor` = (project in file("common/interface-adaptor"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-interface-adaptor",
    libraryDependencies ++= Seq(
      typesafeAkka.akkaHttp,
      typesafeAkka.akkaStreamTyped,
      heikoseeberger.akkaHttpCirce,
      scalatest.scalatest            % Test,
      typesafeAkka.actorTestkitTyped % Test,
      typesafeAkka.httpTestkit       % Test,
      logback.logbackClassic         % Test
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  ).dependsOn(`infrastructure-scala`)

lazy val `domain-scala` = (project in file("scala/domain-scala"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-domain-scala",
    libraryDependencies ++= Seq(
      airframe.ulid
    )
  ).dependsOn(`infrastructure-scala`)

lazy val `test-base-scala` = (project in file("scala/test-base-scala"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-test-base",
    libraryDependencies ++= Seq(
      iheart.ficus,
      typesafeAkka.akkaActorTyped,
      typesafeAkka.actorTestkitTyped,
      scalatest.scalatest,
      "com.github.j5ik2o" %% "docker-controller-scala-scalatest"      % "1.14.119",
      "com.github.j5ik2o" %% "docker-controller-scala-localstack"     % "1.14.119",
      "com.github.j5ik2o" %% "docker-controller-scala-dynamodb-local" % "1.14.119",
      "com.github.j5ik2o" %% "docker-controller-scala-mysql"          % "1.14.119",
      "com.github.j5ik2o" %% "docker-controller-scala-flyway"         % "1.14.119",
      awssdk.v1.dynamodb,
      "com.typesafe.slick" %% "slick" % "3.4.1"
    )
  ).dependsOn(`infrastructure-scala`)

lazy val `read-model-updater-base` = (project in file("common/read-model-updater-base"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-read-model-updater-base",
    libraryDependencies ++= Seq(
      iheart.ficus,
      j5ik2o.akkaKinesisKclDynamoDBStreams,
      typesafeAkka.akkaActorTyped,
      airframe.ulid,
      mockito.mocktioScala % Test,
      scalatest.scalatest  % Test
    )
  )

lazy val `read-model-updater-scala` = (project in file("scala/read-model-updater-scala"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .settings(
    name := "adceet-read-model-updater-scala",
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  ).settings(
    libraryDependencies ++= Seq(
      typesafeAkka.akkaPersistenceTyped,
      typesafeAkka.akkaSerializationJackson,
      fasterXmlJackson.scala,
      awssdk.v1.sts,
      awssdk.v2.sts,
      logback.logbackClassic,
      "jakarta.xml.bind" % "jakarta.xml.bind-api" % "4.0.0",
      "com.sun.xml.bind" % "jaxb-impl"            % "4.0.1"
    )
  )
  .dependsOn(
    `read-model-updater-base` % "compile->compile;test->test",
    `test-base-scala`         % "test",
    `write-api-server-scala`  % "test->test",
    `read-api-base-scala`,
    `domain-scala`,
    `interface-adaptor` % "compile->compile;test->test"
  )

lazy val `read-api-base-scala` = (project in file("scala/read-api-base-scala"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-adceet-read-api-base",
    libraryDependencies ++= Seq(
      iheart.ficus,
      airframe.di,
      "com.typesafe.slick" %% "slick"                % "3.4.1",
      "com.typesafe.slick" %% "slick-hikaricp"       % "3.4.1",
      "mysql"               % "mysql-connector-java" % "8.0.30",
      megard.akkaHttpCors,
      typesafeAkka.akkaHttp,
      heikoseeberger.akkaHttpCirce,
      typesafeAkka.akkaHttpSprayJson,
      typesafeAkka.akkaHttpJackson,
      typesafeAkka.akkaSerializationJackson,
      typesafeAkka.akkaSlf4j,
      typesafeAkka.akkaStreamTyped,
      fasterXmlJackson.scala
    )
  ).dependsOn(`interface-adaptor`)

lazy val `read-api-server-scala` = (project in file("scala/read-api-server-scala"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .settings(
    name := "adceet-read-api-server-scala",
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  ).settings(
    libraryDependencies ++= Seq(
      logback.logbackClassic,
      circre.core,
      circre.generic,
      circre.parser,
      kamon.statusPage,
      kamon.akka,
      kamon.akkaHttp,
      kamon.systemMetrics,
      kamon.datadog,
      "com.github.scopt" %% "scopt"      % "4.0.1",
      "com.beachape"     %% "enumeratum" % "1.7.2",
      swaggerAkkaHttp.swaggerAkkaHttp,
      awssdk.v1.sts,
      awssdk.v2.sts,
      mockito.mocktioScala       % Test,
      scalatest.scalatest        % Test,
      typesafeAkka.streamTestkit % Test,
      typesafeAkka.httpTestkit   % Test
    )
  )
  .dependsOn(`read-api-base-scala`)

lazy val `write-api-base` = (project in file("common/write-api-base"))
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings
  ).settings(
    name := "adceet-write-api-base",
    libraryDependencies ++= Seq(
      iheart.ficus,
      airframe.di,
      typesafeAkka.akkaSlf4j,
      typesafeAkka.akkaPersistenceTyped,
      awssdk.v1.sts,
      awssdk.v2.sts,
      j5ik2o.akkaPersistenceDynamoDBJournalV1,
      j5ik2o.akkaPersistenceDynamoDBJournalV2,
      j5ik2o.akkaPersistenceDynamoDBSnapshotV1,
      j5ik2o.akkaPersistenceDynamoDBSnapshotV2,
      kamon.core,
      logback.logbackClassic,
      jakarta.rsApi,
      swaggerAkkaHttp.swaggerAkkaHttp,
      circre.core,
      circre.generic,
      circre.parser,
      megard.akkaHttpCors,
      typesafeAkka.akkaHttp,
      heikoseeberger.akkaHttpCirce,
      typesafeAkka.akkaHttpSprayJson,
      typesafeAkka.akkaHttpJackson,
      typesafeAkka.akkaSlf4j,
      typesafeAkka.akkaActorTyped,
      typesafeAkka.akkaStreamTyped,
      typesafeAkka.akkaClusterTyped,
      typesafeAkka.akkaClusterShardingTyped,
      typesafeAkka.akkaSerializationJackson,
      typesafeAkka.akkaDiscovery,
      lightbend.akkaManagement,
      lightbend.akkaManagementClusterHttp,
      lightbend.akkaManagementClusterBootstrap,
      lightbend.discoveryK8sApi,
      fasterXmlJackson.scala,
      kamon.statusPage,
      kamon.akka,
      kamon.akkaHttp,
      kamon.systemMetrics,
      kamon.datadog,
      aichler.jupiterInterface(JupiterKeys.jupiterVersion.value) % Test,
      mockito.mocktioScala                                       % Test,
      scalatest.scalatest                                        % Test,
      jupiter.jupiterApi                                         % Test,
      jupiter.jupiter                                            % Test,
      jupiter.jupiterMigrationSupport                            % Test,
      typesafeAkka.actorTestkitTyped                             % Test,
      typesafeAkka.streamTestkit                                 % Test,
      typesafeAkka.httpTestkit                                   % Test,
      typesafeAkka.multiNodeTestkit                              % Test,
      awaitility.awaitility                                      % Test,
      commonsIO.commonsIO                                        % Test,
      // テストでは使っていないので削除してもよい
      fusesource.leveldbjniAll % Test,
      iq80LevelDb.leveldb      % Test
    )
  ).dependsOn(`infrastructure-scala`)

lazy val `write-api-server-scala` = (project in file("scala/write-api-server-scala"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .configs(MultiJvm)
  .settings(
    Settings.baseSettings,
    Settings.scalaSettings,
    Settings.javaSettings,
    Settings.multiJvmSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  )
  .settings(
    name := "adceet-write-api-server-scala",
    Compile / run / mainClass := Some("com.github.j5ik2o.adceet.api.write.Main"),
    dockerEntrypoint := Seq(s"/opt/docker/bin/${name.value}"),
    dockerExposedPorts := Seq(8081, 8558, 25520),
    javaAgents += "io.kamon" % "kanela-agent" % "1.0.17",
    run / javaOptions ++= Seq(
      s"-Dcom.sun.management.jmxremote.port=${sys.env.getOrElse("JMX_PORT", "8999")}",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.local.only=false",
      "-Dcom.sun.management.jmxremote",
      "-Xms1024m",
      "-Xmx1024m",
      "-Djava.library.path=./target/native"
    ),
    Universal / javaOptions ++= Seq(
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.local.only=true",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dorg.aspectj.tracing.factory=default"
    ),
    Test / publishArtifact := false,
    run / fork := false,
    Test / parallelExecution := false,
    Global / cancelable := false,
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt"      % "4.0.1",
      "com.beachape"     %% "enumeratum" % "1.7.2"
    )
  ).dependsOn(`write-api-base` % "compile->compile;test->test", `domain-scala`, `test-base-scala` % "test")

lazy val `write-api-server-kotlin` = (project in file("kotlin/write-api-server-kotlin"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .configs(MultiJvm)
  .settings(
    Settings.baseSettings,
    Settings.javaSettings,
    Settings.scalaSettings,
    Settings.kotlinSettings,
    Settings.multiJvmSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  )
  .settings(
    name := "adceet-write-api-server-kotlin",
    Compile / run / mainClass := Some("com.github.j5ik2o.adceet.api.write.Main"),
    dockerEntrypoint := Seq(s"/opt/docker/bin/${name.value}"),
    dockerExposedPorts := Seq(8081, 8558, 25520),
    javaAgents += "io.kamon" % "kanela-agent" % "1.0.17",
    run / javaOptions ++= Seq(
      s"-Dcom.sun.management.jmxremote.port=${sys.env.getOrElse("JMX_PORT", "8999")}",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.local.only=false",
      "-Dcom.sun.management.jmxremote",
      "-Xms1024m",
      "-Xmx1024m",
      "-Djava.library.path=./target/native"
    ),
    Universal / javaOptions ++= Seq(
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.local.only=true",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dorg.aspectj.tracing.factory=default"
    ),
    // for Kotlin
    libraryDependencies ++= Seq(
      fasterXmlJackson.kotlin,
      kodeinDI.kodeinDIJvm,
      kotlinx.coroutinesCoreJvm,
      xenomachina.kotlinArgParser,
      arrowKt.arrowCore,
      vavr.vavrKotlin,
      mockk.mockk            % Test,
      kotlinx.coroutinesTest % Test
    ),
    Test / publishArtifact := false,
    run / fork := false,
    Test / parallelExecution := false,
    Global / cancelable := false
  ).dependsOn(`write-api-base` % "compile->compile;test->test")

lazy val `write-api-server-java` = (project in file("java/write-api-server-java"))
  .enablePlugins(JavaAgent, JavaAppPackaging, EcrPlugin, MultiJvmPlugin)
  .disablePlugins(KotlinPlugin)
  .configs(MultiJvm)
  .settings(
    Settings.baseSettings,
    Settings.javaSettings,
    Settings.scalaSettings,
    Settings.multiJvmSettings,
    Settings.dockerCommonSettings,
    Settings.ecrSettings
  )
  .settings(
    name := "adceet-write-api-server-java",
    Compile / run / mainClass := Some("com.github.j5ik2o.adceet.api.write.Main"),
    dockerEntrypoint := Seq(s"/opt/docker/bin/${name.value}"),
    dockerExposedPorts := Seq(8081, 8558, 25520),
    javaAgents += "io.kamon" % "kanela-agent" % "1.0.17",
    run / javaOptions ++= Seq(
      s"-Dcom.sun.management.jmxremote.port=${sys.env.getOrElse("JMX_PORT", "8999")}",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dcom.sun.management.jmxremote.ssl=false",
      "-Dcom.sun.management.jmxremote.local.only=false",
      "-Dcom.sun.management.jmxremote",
      "-Xms1024m",
      "-Xmx1024m",
      "-Djava.library.path=./target/native"
    ),
    Universal / javaOptions ++= Seq(
      "-Dcom.sun.management.jmxremote",
      "-Dcom.sun.management.jmxremote.local.only=true",
      "-Dcom.sun.management.jmxremote.authenticate=false",
      "-Dorg.aspectj.tracing.factory=default"
    ),
    // for Java
    libraryDependencies ++= Seq(
      vavr.vavr,
      "com.google.inject"  % "guice"          % "5.1.0",
      "org.functionaljava" % "functionaljava" % "5.0",
      "org.mockito"        % "mockito-core"   % "5.1.0" % Test
    ),
    Test / publishArtifact := false,
    run / fork := false,
    Test / parallelExecution := false,
    Global / cancelable := false
  ).dependsOn(`write-api-base` % "compile->compile;test->test")

// --- Custom commands
addCommandAlias(
  "lint",
  ";write-api-server-java/javafmtCheckAll;write-api-server-kotlin/spotlessCheck;scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck;scalafixAll --check"
)
addCommandAlias(
  "fmt",
  ";write-api-server-java/javafmtAll;write-api-server-kotlin/spotlessApply;scalafmtAll;scalafmtSbt;scalafix RemoveUnused"
)
