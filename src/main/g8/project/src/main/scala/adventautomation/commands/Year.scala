package adventautomation.commands

import sbt._
import Keys._
import complete.DefaultParsers._

import java.util.{Calendar, GregorianCalendar, TimeZone}
import scala.util.{Try, Success, Failure}
import adventautomation.utils._
import Logging._

// is this the right way to write custom commands?
// absolutley not 
// but im doing it anyway
object Year {
  def year = Command.args("year", "<year>") { (state, args) =>
    val foundYear = SettingsManager.get("year").get

    def getYear(fnd: String) = {
      fnd match {
        case yr if Try(yr.toInt).isSuccess => println(s"[info] year is currently set to $yr")
        case _ => println("[info] year has not been set, please do so with 'year set <year>'")
      } 
      state
    }

    def resetYear = {
      SettingsManager.set("year", "N/A")
      println("[info] year has been reset.")
      state.reload
    }

    args.toList match {
      case Nil => getYear(foundYear)

      case command :: Nil => command match {
        case "reset" => resetYear
        case "get" => getYear(foundYear)
        case "set" => {
          error("Please provide a year")
          state
        }
        case _ => { 
          error(s"Invalid command: $command")
          state
        }
      }

      case command :: year :: rest => {
        command match {
          case "reset" => resetYear
          case "get" => getYear(foundYear)
          case "set" => {
            val currentYear = (new GregorianCalendar).get(Calendar.YEAR)
            val currentMonth = {
              val cal = new GregorianCalendar
              cal.setTimeZone(TimeZone.getTimeZone("EST"))
              cal.get(Calendar.MONTH)
            }
            def isValidYear(yr: String) = Try(yr.toInt).isSuccess && 2015 <= yr.toInt && yr.toInt <= currentYear
            year match {
              case yr if yr == foundYear => {
                error(s"Year already set to $yr")
                state
              }
              case yr if isValidYear(yr) => {
                SettingsManager.set("year", yr)
                if (yr.toInt == currentYear && currentMonth < 12) {
                  warn("It doesn't appear to be December (in EST) yet. Please note that fetching data for this year will not work until then")
                }
                suc(s"Year has been set to $yr")
                state.reload
              }
              case _ => {
                error(s"Please provide a valid year between 2015 and $currentYear")
                state
              }
            }
          }
          case _ => {
            error(s"Invalid command: $command.")
            state
          }
        } 
      }     
    }
  }
}