name := "aoc"

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "io.github.daghemberg" %% "paut-program" % "0.1.4",
      "io.github.daghemberg" %% "problemutils" % "0.1.1",
      // add your own dependencies here!
    )
  )
