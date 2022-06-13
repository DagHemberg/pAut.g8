package adventautomation.plugins

import sbt._
import Keys._
import complete.DefaultParsers._
import scala.util.{Try, Success, Failure}
import adventautomation.utils._
import Logging._
import DayInputHandler._
import SettingsManager._
import scala.Console._
import java.util.{GregorianCalendar, Calendar}
import scala.io.Source

object Submit extends AutoPlugin {
  object autoImport {
    val submit = inputKey[Unit]("Submits the solution for a given day")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    submit := {
      val args = spaceDelimited("<day> <part> [year]").parsed.toList
      val token = SettingsManager.get("token").get

      def submitAnswer(day: Int, part: String, year: String = SettingsManager.get("year").getOrElse("N/A")) = {
        import sys.process._
        if (year != "N/A") {
          getAnswer(day, part, year) match {
            case Right(answer) => {
              val mode = "-X POST"
              val agent = "-A 'Scala AdventAutomation v0.1'"
              val desc = s"-d 'level=$part&answer=$answer'"
              val auth = s"--cookie 'session=$token'"
              val url = s"https://adventofcode.com/$year/day/$day/answer"
              
              inf(s"Submitting answer for day $day, part $part in year $year...")
  
              val command = s"curl --silent $agent $mode $desc $auth $url"
              val result = s"""bash -c "$command"""".!!

              result match {
                case res if res startsWith "404" => 
                  error("This day doesn't seem to exist yet. Try again later.")
                
                case res if res contains "That's the right answer" => 
                  val file = new File("./src/main/resources/results.csv")
                  val current = Source
                    .fromFile("./src/main/resources/results.csv").getLines()
                    .toVector
                  IO.write(
                    file, 
                    current.map(line => 
                      if (line contains s"$year;${f"$day%02d"};$part") line.replace("not submitted", "submitted") 
                      else line
                    ).mkString("", "\n", "\n")
                  )
                  suc(s"${YELLOW}Correct${RESET} answer!") 
                  suc(s"Head to ${
                    part match {
                      case "1" => s"${CYAN}https://adventofcode.com/$year/day/$day#part2${RESET}"
                      case "2" => s"${CYAN}https://adventofcode.com/$year/day/${day + 1}${RESET}"
                    }
                  } to continue on your journey!")
  
                case res if res contains "That's not the right answer" => 
                  error(s"That's not the right answer.")
                  res match {
                    case hint if hint contains "too low" => 
                      error(s"Hint: Your answer was ${YELLOW}too low${RESET}.")

                    case hint if hint contains "too high" => 
                      error(s"Hint: Your answer was ${YELLOW}too high${RESET}.")

                    case hint => error(s"No hint was given.")
                  }
                  val time = "(?<=  Please wait )(.*)(?= before)".r.findFirstIn(res).getOrElse("N/A")
                  error(s"Please wait $time before submitting another answer.")

                case res if res contains "You don't seem to be solving the right level" => {
                  error("You don't seem to be solving the right level. Either you don't have access to this problem yet or you've already solved it.")
                }

                case res if res contains "too recently" => {
                  val timeLeft = "(?<=  You have )(.*)(?= left to wait)".r.findFirstIn(res).getOrElse("N/A")
                  error(s"You submitted an answer too recently. Please wait ${timeLeft} before submitting again.")
                }

                case res if res.isEmpty => 
                  error("Something went wrong when submitting answer.")
              }
            }
            case Left("not found") => 
              error(s"No answer found for day $day, part $part in year $year")
            case Left("already submitted") =>
              error(s"Answer for day $day, part $part in year $year has already been submitted")
            case Left(err) => 
              error(s"idk what happened. $err")
          }
        } else {
          error("Year not specified. Please either set the year using 'year set <year>'")
          error("or manually provide the year at the end of the command ('submit <day> <part> [year]').")
        }
      }

      def getAnswer(day: Int, part: String, year: String): Either[String, String] = {
        val current = Source
          .fromFile("./src/main/resources/results.csv")
          .getLines()
          .toVector
        
        val d = f"$day%02d"

        current
          .tail
          .map(_.split(";").toList)
          .collect { case _ :: `year` :: `d` :: `part` :: _ :: result :: status :: Nil => (status, result) }
          .headOption match {
            case Some(("not submitted", result: String)) => Right(result)
            case Some(("submitted", _)) => Left("already submitted")
            case None | Some(_) => Left("not found")
          }
      }

      def handleArgs(restArgs: List[String], part: String) = {
        // dont try this at home, kids
        handleDayInput(args) { (day, _) => 
          if (token.isEmpty) error("Token not set, please set it with 'set token <token>'")
          else {
            restArgs match {
              case Nil => submitAnswer(day, part)
              case year :: _ => {
                val currentYear = (new GregorianCalendar).get(Calendar.YEAR)
                Try {
                  val y = year.toInt
                  require(2015 <= y && y <= currentYear)
                } match {
                  case Failure(_) => error(s"Please provide a year between 2015 and $currentYear (or leave empty to use the currently set year)")
                  case Success(_) => submitAnswer(day, part, year)
                }
              }
            }
          }
        }
      }

      args match {
        case Nil => error("Arguments missing: <day> <part> [year]")
        case day :: Nil => error("Argument missing: <part> [year]")
        case day :: part :: yearAndRest => handleArgs(yearAndRest, part)
      }
    }
  )
}