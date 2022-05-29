resolvers ++= Seq(
  "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Release Repository" at "https://oss.sonatype.org/content/repositories/releases/",
  Resolver.bintrayRepo("kamon-io", "sbt-plugins"),
  Resolver.jcenterRepo
)

addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.6")

addSbtPlugin("com.mintbeans" % "sbt-ecr" % "0.16.0")

addSbtPlugin("net.aichler" % "sbt-jupiter-interface" % "0.8.3")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

addSbtPlugin("com.hanhuy.sbt" % "kotlin-plugin" % "2.0.0")

addSbtPlugin("net.moznion.sbt" % "sbt-spotless" % "0.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.0")
