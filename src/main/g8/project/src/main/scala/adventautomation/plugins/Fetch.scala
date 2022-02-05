package adventautomation.plugins

import sbt._
import Keys._
import complete.DefaultParsers._
import scala.util.{Try, Success, Failure}
import adventautomation.utils._
import Logging._
import DayInputHandler._

object Fetch extends AutoPlugin {
  object autoImport {
    val fetch = inputKey[Unit]("Fetches the input data for a new day")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    fetch := {
      import sys.process._
      handleDayInput(spaceDelimited("<day>").parsed) { (uday, _) =>
        val day = f"${uday}%02d"
        val year = SettingsManager.get("year").getOrElse("N/A")
        if (year == "N/A") {
          error("Please set the year using 'year set <year>' before fetching the input data.")
        } else {
          val sampleFile = file(s"src/main/resources/input/examples/$year/$day.txt")
          val puzzleFile = file(s"src/main/resources/input/puzzles/$year/$day.txt")
          if (Seq(sampleFile, puzzleFile).exists(_.exists())) {
            error(s"Data for day $uday has already been fetched")
          } else {
            val cookie = SettingsManager.get("token").getOrElse("")
            val url = s"https://adventofcode.com/$year/day/$uday/input"
            val userAgent = "Scala AdventOfCode Helper v0.1"
            val command = s"curl --silent -A '$userAgent' --cookie 'session=$cookie' '$url'"
            val input = s"""bash -c "${command}"""".!!
  
            input match {
  
              case data if data.startsWith("Puzzle inputs differ by user") => {
                error("Could not authenticate with the AoC website")
                error("You might need to update your session token: run 'auth set <sessionToken>'")
              }
  
              case data if data.startsWith("404") => {
                error(s"Day $day in year $year doesn't appear to exist on AoC yet. Try again later? (But don't spam!)")
              }
  
              case data => {
                IO.write(puzzleFile, data)
                IO.write(sampleFile, s"Please paste the example input for day $day here!")
                sys.props("os.name") match {
                  case os if os.startsWith("Windows") => s"cmd /c start ${sampleFile.getPath()}".!!
                  case os if os.toLowerCase.contains("mac") => s"open ${sampleFile.getPath()}".!! // not tested
                  case _ => {
                    warn(s"Couldn't open $sampleFile in your default text editor")
                    warn("Please edit the file manually to provide example input to the problems.")
                  } 
                }
                suc(s"Fetched data for day $day")
              }
            }
          }
        }
      }
    }
  )
}