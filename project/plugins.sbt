resolvers ++= Seq(
  "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/",
  Resolver.bintrayRepo("kamon-io", "sbt-plugins"),
  Resolver.jcenterRepo,
  "jitpack" at "https://jitpack.io"
)

libraryDependencies ++= {
  val amazonSdkV = "1.11.672"
  val scalaTestV = "3.0.8"
  val awsSsoSdkV = "2.16.63"
  Seq(
    "com.amazonaws"          % "aws-java-sdk-sts" % amazonSdkV,
    "com.amazonaws"          % "aws-java-sdk-ecr" % amazonSdkV,
    "software.amazon.awssdk" % "sso"              % awsSsoSdkV,
    "org.scalatest"         %% "scalatest"        % scalaTestV % "test",
    "com.github.tmtsoftware" % "kotlin-plugin"    % "3.0.4"
  )
}

addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.6")

// addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.16.0")
//lazy val root       = project.in(file(".")).dependsOn(githubRepo)
//lazy val githubRepo = RootProject(uri("git://github.com/crossroad0201/sbt-ecr.git#69d8121"))

addSbtPlugin("net.aichler" % "sbt-jupiter-interface" % "0.8.3")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

// addSbtPlugin("com.github.tmtsoftware" % "kotlin-plugin" % "3.0.3")

addSbtPlugin("net.moznion.sbt" % "sbt-spotless" % "0.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.4")

addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.8.0")

addSbtPlugin("io.github.sbt-dao-generator" % "sbt-dao-generator" % "1.3.0")
