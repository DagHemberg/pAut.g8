package adventautomation.plugins

import sbt._
import Keys._
import complete.DefaultParsers._
import scala.util.{Try, Success, Failure}
import adventautomation.utils._
import Logging._
import DayInputHandler._

object Init extends AutoPlugin {
  object autoImport {
    val init = inputKey[Unit]("Initializes a new day")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    init := {
      def createFile(file: File, content: String) = {
        if (file.exists()) {
          inf(s"File ${file.getPath()} already exists, skipping...")
        } else {
          IO.write(file, content)
          inf(s"Created file ${file.getPath()}")
        }
      }

      handleDayInput(spaceDelimited("<day> [name]").parsed) { (uDay, nameArgs) =>
        val year = SettingsManager.get("year").getOrElse("N/A")
        if (year == "N/A") {
          error("Please set the year with 'year set <year>' beofre initalizing days")
        } else {
          val day = f"${uDay}%02d"
          val folder = s"src/main/scala/aoc/$year/day$day${
            if (nameArgs.isEmpty) "" 
            else s"-${nameArgs.mkString("-").toLowerCase}"
          }"
          val pkg = file(s"$folder/package.scala")
          def part(n: Int) = file(s"$folder/Part$n.scala")
          val testing = file(s"$folder/testing.worksheet.sc")

          inf(s"Initializing day $day...")
          
          createFile(testing, 
            IO.read(file("src/main/resources/templates/testing.txt"))
              .replace("@DAY", day)
              .replace("@YEAR", year))

          createFile(pkg, 
            IO.read(file("src/main/resources/templates/package.txt"))
              .replace("@DAY", day)
              .replace("@YEAR", year))
          
          for (n <- 1 to 2) { 
            createFile(part(n), IO
              .read(file("src/main/resources/templates/problem.txt"))
              .replace("@PART", n.toString)
              .replace("@DAY", day)
              .replace("@NAME", if (nameArgs.isEmpty) "N/A" else nameArgs.mkString(" "))
              .replace("@YEAR", year)
            )
          
            // IO.write(
            //   file("src/main/scala/aoc/utils/ProblemList.scala"), 
            //   s"list += aoc.y${year}.day${day}.Part${n}\n    ", 
            //   append = true
            // )
          }
        }
      }
    }
  )
}