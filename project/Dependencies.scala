import sbt._

object Dependencies {
  private val slf4jVersion = "1.7.30"
  private val log4jVersion = "2.13.1"

  lazy val overrides: Seq[ModuleID] = Seq()

  lazy val dependencies: Seq[ModuleID] = Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion, // The Log4j 2 SLF4J Binding allows applications coded to the SLF4J API to use Log4j 2 as the implementation.
    "org.apache.logging.log4j" % "log4j-core" % log4jVersion, // configured via our log4j2.xml. Note that log4j-api is not needed as we don't use it directly. It will be pulled in transitively (log4j-slf4j-impl uses it)
    "tech.sparse" %% "toml-scala" % "0.2.2",
  )

  lazy val testLibraries: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.1.0",
  ).map(_ % Test)}
