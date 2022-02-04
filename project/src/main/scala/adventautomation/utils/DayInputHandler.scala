package adventautomation.utils

import util.{Try, Success, Failure}
import Logging._

object DayInputHandler {
  def handleDayInput(args: Seq[String])(action: (Int, Seq[String]) => Unit) = {
    args.toList match {
      case Nil => error("Please specify a day")
      case unformattedDay :: restArgs => {
        Try {
          val day = unformattedDay.toInt
          require(1 <= day && day <= 25)
          (day, restArgs.toSeq)
        } match {
          case Failure(e) => {
            error(s"Invalid day: $unformattedDay. Please specify a day between 1 and 25")
          } 
          case Success((day, args)) => action(day, args)
        }
      }
    }
  }
}