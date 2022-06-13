import adventautomation.plugins._
import adventautomation.utils.SettingsManager
import adventautomation.commands.Year._

val scala3Version = "3.1.2"

lazy val projectName = s"aoc-${SettingsManager.get("year").getOrElse("N/A")}"

lazy val root = project
  .in(file("."))
  .enablePlugins(Auth, Fetch, Init, Submit, Stats)
  .enablePlugins(SiteScaladocPlugin, GhpagesPlugin)
  .settings(
    SiteScaladoc / siteSubdirName := "api/latest",
    git.remoteRepo := "git@github.com:DagHemberg/AdventAutomation.g8.git",
    name := projectName,
    version := "0.1",
    scalaVersion := scala3Version,
    commands ++= Seq(year),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.8.0"
      // add your library dependencies here!
    ),
  )
