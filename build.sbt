import Dependencies._
val mainClassName = "net.michaelripley.socialsecurity.SocialSecurityMath"
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.13.1"
    )),
    organization := "net.michaelripley.socialsecurity",
    name := "social-security-maximization",
    version := "0.0.1",
    dependencyOverrides ++= overrides,
    libraryDependencies ++= dependencies,
    libraryDependencies ++= testLibraries,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    mainClass in(Compile, run) := Some(mainClassName),
    mainClass in assembly := Some(mainClassName),
  )
