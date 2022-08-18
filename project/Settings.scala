import com.amazonaws.regions.{ Region, Regions }
import com.github.sbt.git.SbtGit.git
import com.typesafe.sbt.SbtNativePackager.autoImport.packageName
import com.typesafe.sbt.packager.Keys.daemonUser
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker._
import kotlin.Keys.{ kotlinLib, kotlinVersion, kotlincOptions }
import net.aichler.jupiter.sbt.Import.jupiterTestFramework
import net.moznion.sbt.SbtSpotless.autoImport.spotlessKotlin
import net.moznion.sbt.spotless.config.{ KotlinConfig, KtlintConfig }
import sbt.Keys._
import sbt.{ Def, _ }
import sbtecr.EcrPlugin.autoImport._
import scalafix.sbt.ScalafixPlugin.autoImport.{ scalafixScalaBinaryVersion, scalafixSemanticdb }

object Settings {

  val kotlinSettings: Seq[Def.Setting[_]] = Seq(
    kotlinVersion := "1.6.21",
    kotlincOptions ++= Seq("-jvm-target", "11"),
    kotlinLib("stdlib-jdk8"),
    kotlinLib("reflect"),
    spotlessKotlin := KotlinConfig(
      target = Seq("src/**/*.kt", "test/**/*.kt"),
      ktlint = KtlintConfig(version = "0.40.0", userData = Map("indent_size" -> "2", "continuation_indent_size" -> "2"))
    )
  )

  val javaSettings: Seq[Def.Setting[_]] = Seq(
    Compile / javacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    )
  )

  val scalaSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := "2.13.8",
    Compile / scalacOptions ++= Seq(
      "-target:jvm-11",
      "-encoding",
      "UTF-8",
      "-language:_",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlog-reflective-calls",
      "-Xlint"
    ),
    ThisBuild / semanticdbEnabled := true,
    ThisBuild / semanticdbVersion := scalafixSemanticdb.revision,
    ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
  )

  val baseSettings: Seq[Def.Setting[_]] = Seq(
    organization := "com.github.j5ik2o",
    version := "1.0.0-SNAPSHOT",
    testOptions += Tests.Argument(jupiterTestFramework, "-q", "-v"),
    resolvers ++= Seq(
      "jitpack" at "https://jitpack.io",
      Resolver.jcenterRepo,
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases")
    )
  )

  private object EcrRepositorySetting {
    val Prefix: String          = sys.env.getOrElse("PREFIX", "dummy")
    val ApplicationName: String = sys.env.getOrElse("APPLICATION_NAME", "dummy")
    val AwsAccountId: String    = sys.env.getOrElse("AWS_ACCOUNT_ID", "111111111111")
    val AwsRegion: String       = sys.env.getOrElse("AWS_REGION", "ap-northeast-1")
    val RepositoryUri: String   = s"$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com"

    def repositoryNameForProject(projectName: String): String = s"$Prefix-ecr-$ApplicationName-$projectName"
  }

  val dockerCommonSettings: Seq[Def.Setting[_]] = Seq(
    dockerBaseImage := "adoptopenjdk/openjdk11:jdk-11.0.12_7-slim",
    packageDoc / publishArtifact := false,
    Docker / version := git.gitHeadCommit.value.get,
    // sbt docker:publishLocal でローカルにDockerイメージを作成するときも、ECRリポジトリ上のパッケージ名と同じになるようにする
    // Docker / packageName := s"${EcrRepositorySetting.RepositoryUri}/${EcrRepositorySetting.repositoryNameForProject(name.value)}",
    dockerUpdateLatest := true, // docker:publishLocal でローカルにイメージを保存するときは latest タグを更新する
    bashScriptExtraDefines ++= Seq(
      "addJava -Xms${JVM_HEAP_MIN:-1024m}",
      "addJava -Xmx${JVM_HEAP_MAX:-1024m}",
      "addJava -XX:MaxMetaspaceSize=${JVM_META_MAX:-512M}",
      "addJava ${JVM_GC_OPTIONS:--XX:+UseG1GC}",
      "addJava -Dconfig.resource=${CONFIG_RESOURCE:-application.conf}",
      "addJava -Dakka.remote.startup-timeout=60s"
    ),
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      // KamonでHostMetrics収集のための依存パッケージ (https://gitter.im/kamon-io/Kamon?at=5dce8551c26e8923c42bba9f)
      ExecCmd("RUN", "apt", "update"),
      ExecCmd("RUN", "apt", "install", "-y", "udev")
    ),
    Docker / daemonUser := "daemon", // https://www.scala-sbt.org/sbt-native-packager/formats/docker.html#daemon-user
    dockerBuildCommand := {
      if (sys.props("os.arch") != "amd64" && sys.env.getOrElse("TARGET_AMD64", "0") == "1") {
        dockerExecCommand.value ++ Seq(
          "buildx",
          "build",
          "--platform=linux/amd64",
          "--load"
        ) ++ dockerBuildOptions.value :+ "."
      } else dockerBuildCommand.value
    }
  )

  def ecrSettings: Seq[Def.Setting[_]] = {
    Seq(
      Ecr / region := Region.getRegion(Regions.fromName(EcrRepositorySetting.AwsRegion)),
      Ecr / repositoryName := EcrRepositorySetting.repositoryNameForProject(name.value),
      // Ecr / localDockerImage := (Docker / packageName).value + ":" + (Docker / version).value,
      Ecr / localDockerImage := (Docker / packageName).value + ":" + (Docker / version).value,
      // sbt ecr:push でECRにイメージをPUSHする場合のイメージタグ
      // * タグは "dev-$Gitコミットハッシュ"
      // * latest タグは付与しない
      // * sbt起動時のタイムスタンプが使用されるため、sbtを起動したままにしているとタグが変わりません。 sbt>reload するとタグが変わります
      Ecr / repositoryTags := Seq("dev-" + (Docker / version).value + "-" + System.currentTimeMillis()),
      Ecr / push := ((Ecr / push) dependsOn (Docker / publishLocal, Ecr / login)).value
    )
  }

  val multiJvmSettings: Seq[Def.Setting[_]] =
    com.typesafe.sbt.SbtMultiJvm.multiJvmSettings

}
