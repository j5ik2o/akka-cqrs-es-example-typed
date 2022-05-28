import com.amazonaws.regions.{ Region, Regions }
import com.github.sbt.git.SbtGit.git
import com.typesafe.sbt.SbtNativePackager.autoImport.packageName
import com.typesafe.sbt.packager.Keys.daemonUser
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptPlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker._
import sbt.Keys._
import sbt.{ Def, _ }
import sbtecr.EcrPlugin.autoImport._

object Settings {
  val baseSettings: Seq[Def.Setting[Seq[Resolver]]] = Seq(
    resolvers ++= Seq(
      "jitpack" at "https://jitpack.io",
      Resolver.jcenterRepo,
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases")
    )
  )
  private object EcrRepositorySetting {
    val Prefix: String                                        = sys.env.getOrElse("PREFIX", "dummy-prefix")
    val AwsAccountId: String                                  = sys.env.getOrElse("AWS_ACCOUNT_ID", "111111111111")
    val AwsRegion: String                                     = sys.env.getOrElse("AWS_REGION", "ap-northeast-1")
    val RepositoryUri: String                                 = s"$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com"
    def repositoryNameForProject(projectName: String): String = s"$Prefix-ecr-$projectName"
  }

  val dockerCommonSettings = Seq(
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
    Docker / daemonUser := "daemon" // https://www.scala-sbt.org/sbt-native-packager/formats/docker.html#daemon-user
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
