import java.nio.file._
import java.nio.file.attribute._
import macroRevolver._

lazy val baseSettings = Seq(
  organization := "com.bumnetworks",
  version := "0.0.1-JOBCASE-1",
  scalaVersion := "2.11.8",
  initialCommands := """
    import numerato._
    import scala.reflect.runtime.universe._
  """,
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature")) ++ scalariformSettings ++ tutSettings

lazy val deps = Seq(
 libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    "org.specs2" %% "specs2-core" % "3.7.2" % "test",
    "org.specs2" %% "specs2-matcher-extra" % "3.7.2" % "test"))

lazy val publishSettings = Seq(
  publishTo <<= (version) {
    v =>
    val repo = file(".") / ".." / "repo"
    Some(Resolver.file("repo",
      if (v.trim.endsWith("SNAPSHOT")) repo / "snapshots"
      else repo / "releases"))
  }
)

lazy val updateReadme = taskKey[Unit]("copy tut-generated README.md to project root")

lazy val core = project
  .in(file("."))
  .settings(baseSettings)
  .settings(MacroRevolverPlugin.useMacroParadise)
  .settings(deps)
  .settings(name := "numerato", moduleName := "numerato")
  .settings(updateReadme := {
    val README = "README.md"
    tut.value.foreach {
      case (generated, README) =>
        Files.copy(
          Paths.get(generated.toURI),
          Paths.get(baseDirectory.value.toURI).resolve(README),
          StandardCopyOption.REPLACE_EXISTING
        )
      case _ =>
    }
  })
  .settings(publishSettings)

lazy val tests = project
  .in(file("tests"))
  .settings(baseSettings)
  .settings(deps)
  .settings(name := "tests", moduleName := "numerato-tests")
  .settings(publish := {})
  .settings(MacroRevolverPlugin.useMacroParadise)
  .settings(MacroRevolverPlugin.testCleanse)
  .dependsOn(core)

lazy val numerato = project
  .aggregate(core, tests)
  .settings(baseSettings)
