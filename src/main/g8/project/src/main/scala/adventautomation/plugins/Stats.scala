package adventautomation.plugins

import sbt._
import Keys._
import complete.DefaultParsers._
import scala.io.Source
import adventautomation.utils.Logging._

object Stats extends AutoPlugin {
  object autoImport {
    val stats = inputKey[Unit]("Prints out statistics about the submitted projects")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    stats := {
      val args = spaceDelimited("toggle <line>").parsed.toList
      val path = "./src/main/resources/results.csv"
      val lines = Source
        .fromFile(path)
        .getLines()
        .toVector

      args match {
        case "toggle" :: lineRaw :: _ =>
          val lineNum = lineRaw.toInt
          if (lineNum == 0 || lineNum > lines.size) {
            error("Invalid line number")
          } else {
            val line = lines(lineNum)
            val oldStatus = if (line contains "not") "not submitted" else "submitted"
            val newStatus = if (line contains "not") "submitted" else "not submitted"
            IO.write(new File(path), lines.updated(lineNum, line.replace(oldStatus, newStatus)).mkString("", "\n", "\n"))
          }
        case _ => println(
          lines
            .zipWithIndex
            .map { case (l, i) => s"$i  $l" }
            .mkString("\n")
        )
      }
    }
  )
}