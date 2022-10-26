name := "$name$"

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "$scala_version$",
    libraryDependencies ++= Seq(
      "io.github.daghemberg" %% "paut-program" % "0.1.3",
      "io.github.daghemberg" %% "problemutils" % "0.1.1",
      // add your own dependencies here!
    )
  )
