import sbt._

object Version {
  val scalaTest = "3.2.12"
  val akka      = "2.6.19"

  val akkaHttp       = "10.2.9"
  val akkaManagement = "1.1.3"
  val kamon          = "2.6.0"

  val akkaPersistenceDynamoDB = "1.14.152"
  val akkaKinesis             = "1.0.326"
  val cats                    = "2.9.0"
}

object Dependencies {

  object mockito {
    val mocktioScala = "org.mockito" %% "mockito-scala" % "1.17.5"
  }

  object airframe {
    val di   = "org.wvlet.airframe" %% "airframe"      % "23.1.4"
    val ulid = "org.wvlet.airframe" %% "airframe-ulid" % "23.1.4"
  }

  object logback {
    val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.3.1"
  }

  object kodeinDI {
    val kodeinDIJvm = "org.kodein.di" % "kodein-di-jvm" % "7.16.0"
  }
  object arrowKt {
    val arrowCore = "io.arrow-kt" % "arrow-core" % "1.1.3"
  }
  object vavr {
    val vavr       = "io.vavr" % "vavr"        % "0.10.4"
    val vavrKotlin = "io.vavr" % "vavr-kotlin" % "0.10.2"
  }
  object heikoseeberger {
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.39.2"
  }
  object typesafeAkka {
    val akkaHttp          = "com.typesafe.akka" %% "akka-http"            % Version.akkaHttp
    val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
    val akkaHttpJackson   = "com.typesafe.akka" %% "akka-http-jackson"    % Version.akkaHttp

    val akkaSlf4j                = "com.typesafe.akka" %% "akka-slf4j"                  % Version.akka
    val akkaActorTyped           = "com.typesafe.akka" %% "akka-actor-typed"            % Version.akka
    val akkaStreamTyped          = "com.typesafe.akka" %% "akka-stream-typed"           % Version.akka
    val akkaClusterTyped         = "com.typesafe.akka" %% "akka-cluster-typed"          % Version.akka
    val akkaClusterShardingTyped = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Version.akka
    val akkaPersistenceTyped     = "com.typesafe.akka" %% "akka-persistence-typed"      % Version.akka
    val akkaSerializationJackson = "com.typesafe.akka" %% "akka-serialization-jackson"  % Version.akka
    val akkaDiscovery            = "com.typesafe.akka" %% "akka-discovery"              % Version.akka

    val multiNodeTestkit  = "com.typesafe.akka" %% "akka-multi-node-testkit"  % Version.akka
    val actorTestkitTyped = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Version.akka
    val streamTestkit     = "com.typesafe.akka" %% "akka-stream-testkit"      % Version.akka
    val httpTestkit       = "com.typesafe.akka" %% "akka-http-testkit"        % Version.akkaHttp
  }

  object lightbend {
    val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % Version.akkaManagement
    val akkaManagementClusterHttp =
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % Version.akkaManagement
    val akkaManagementClusterBootstrap =
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Version.akkaManagement
    val akkaDiscoveryAwsApiAsync =
      "com.lightbend.akka.discovery" %% "akka-discovery-aws-api-async" % Version.akkaManagement
    val discoveryK8sApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % Version.akkaManagement
  }

  object iheart {
    val ficus = "com.iheart" %% "ficus" % "1.5.2"
  }

  object kotlinx {
    val coroutinesCoreJvm = "org.jetbrains.kotlinx" % "kotlinx-coroutines-core-jvm" % "1.6.1"
    val coroutinesTest    = "org.jetbrains.kotlinx" % "kotlinx-coroutines-test"     % "1.6.1"
  }

  object scalatest {
    val scalatest = "org.scalatest" %% "scalatest" % Version.scalaTest
  }

  object xenomachina {
    val kotlinArgParser = "com.xenomachina" % "kotlin-argparser" % "2.0.6"
  }

  object jakarta {
    val rsApi = "jakarta.ws.rs" % "jakarta.ws.rs-api" % "3.0.0"
  }

  object swaggerAkkaHttp {
    val swaggerAkkaHttp = "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.10.0"
  }

  object megard {
    val akkaHttpCors = "ch.megard" %% "akka-http-cors" % "1.1.3"
  }

  object fasterXmlJackson {
    val scala  = "com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.14.1"
    val kotlin = "com.fasterxml.jackson.module"  % "jackson-module-kotlin" % "2.14.1"
  }

  object circre {
    val circeVersion = "0.14.3"
    val core         = "io.circe" %% "circe-core"    % circeVersion
    val generic      = "io.circe" %% "circe-generic" % circeVersion
    val parser       = "io.circe" %% "circe-parser"  % circeVersion
  }

  object kamon {
    val core          = "io.kamon" %% "kamon-core"           % Version.kamon
    val statusPage    = "io.kamon" %% "kamon-status-page"    % Version.kamon
    val akka          = "io.kamon" %% "kamon-akka"           % Version.kamon
    val akkaHttp      = "io.kamon" %% "kamon-akka-http"      % Version.kamon
    val systemMetrics = "io.kamon" %% "kamon-system-metrics" % Version.kamon
//    val logback       = "io.kamon" %% "kamon-logback"        % Version.kamon
    val datadog = "io.kamon" %% "kamon-datadog" % Version.kamon
//    val apmReporter   = "io.kamon" %% "kamon-apm-reporter"   % Version.kamon
  }

  object jupiter {
    val jupiterApi              = "org.junit.jupiter" % "junit-jupiter-api"              % "5.9.2"
    val jupiter                 = "org.junit.jupiter" % "junit-jupiter"                  % "5.9.2"
    val jupiterMigrationSupport = "org.junit.jupiter" % "junit-jupiter-migrationsupport" % "5.9.2"
  }

  object mockk {
    val mockk = "io.mockk" % "mockk" % "1.12.3"
  }

  object aichler {
    def jupiterInterface(version: String): ModuleID = "net.aichler" % "jupiter-interface" % version
  }

  object awaitility {
    val awaitility = "org.awaitility" % "awaitility" % "4.2.0"
  }

  object commonsIO {
    val commonsIO = "commons-io" % "commons-io" % "2.11.0"
  }

  object fusesource {
    val leveldbjniAll = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  }

  object iq80LevelDb {
    val leveldb = "org.iq80.leveldb" % "leveldb" % "0.12"
  }

  object awssdk {
    object v1 {
      val sts        = "com.amazonaws" % "aws-java-sdk-sts"        % "1.12.377"
      val dynamodb   = "com.amazonaws" % "aws-java-sdk-dynamodb"   % "1.12.395"
      val cloudwatch = "com.amazonaws" % "aws-java-sdk-cloudwatch" % "1.12.395"
      val s3         = "com.amazonaws" % "aws-java-sdk-s3"         % "1.12.329"
      val sqs        = "com.amazonaws" % "aws-java-sdk-sqs"        % "1.12.329"
    }
    object v2 {
      val sts = "software.amazon.awssdk" % "sts" % "2.19.26"
    }
  }

  object j5ik2o {
    val akkaPersistenceDynamoDBJournalV1 =
      "com.github.j5ik2o" %% "akka-persistence-dynamodb-journal-v1" % Version.akkaPersistenceDynamoDB
    val akkaPersistenceDynamoDBJournalV2 =
      "com.github.j5ik2o" %% "akka-persistence-dynamodb-journal-v2" % Version.akkaPersistenceDynamoDB

    val akkaPersistenceDynamoDBSnapshotV1 =
      "com.github.j5ik2o" %% "akka-persistence-dynamodb-snapshot-v1" % Version.akkaPersistenceDynamoDB
    val akkaPersistenceDynamoDBSnapshotV2 =
      "com.github.j5ik2o" %% "akka-persistence-dynamodb-snapshot-v2" % Version.akkaPersistenceDynamoDB

    val akkaKinesisKclDynamoDBStreams = "com.github.j5ik2o" %% "akka-kinesis-kcl-dynamodb-streams" % Version.akkaKinesis
  }

  object cats {
    val core = "org.typelevel" %% "cats-core" % Version.cats
  }

}
