package adventautomation.utils

import sbt._
import Keys._
import java.io.File

object SettingsManager {
  val settingsPath = "src/main/resources/settings.txt"

  private def verify(str: String) = {
    val file = new File(str)
    if (!file.exists()) {
      file.createNewFile()
      IO.write(file, "year=N/A")
    }
  }

  def get(name: String) = {
    verify(settingsPath)
    scala.io.Source
    .fromFile(settingsPath)
    .getLines()
      .find(_.startsWith(s"$name="))
      .flatMap(x => {
        val s = x.split("=")
        if (s.size > 1) Some(s.last) else None
      })
  }

  def set(name: String, value: String) = {
    verify(settingsPath)    
    val settings = scala.io.Source.fromFile(settingsPath).getLines().toVector
    IO.write(
      file(settingsPath), 
      if (settings.find(_.startsWith(s"$name=")).isDefined) {
        settings.map {
          case line if line.startsWith(s"$name=") => s"$name=$value"
          case line => line
        }.mkString("\n")
      } else {
        s"${settings.mkString("\n")}\n$name=$value"
      }
    )
  }
}