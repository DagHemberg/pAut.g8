package aoc.utils

import scala.util.{Try, Success, Failure}
import Console.*
import TimedEval.*

/** An abstract class for solving a problem from Advent of Code. 
  * @param year the year the problem is from
  * @param day the day of the year the problem is from
  * @param expectedExampleSolution the expected solution for the example input
  */
abstract class Problem[A]
  (val year: String, val day: String, val part: String)
  (val expectedExampleSolution: A) extends App:

  def name: String
  def solve(data: Seq[String]): A
  override def toString = s"Day $day: $name"

  private def printlln(x: Any = "")(using printResult: Boolean): Unit = if printResult then println(x)
  
  private def error(msg: String, trim: Boolean = false) =
    s"[${RED}!${RESET}] ${if !trim then s"${RED}Something went wrong${RESET} " else ""}$msg"

  private def success(msg: String) =
    s"[${GREEN}o${RESET}] ${GREEN}$msg${RESET}"

  private def info(msg: String) =
    s"[${CYAN}+${RESET}] $msg"

  private def tinyStack(e: Throwable) =
    s"""|[${RED}!${RESET}] ${e.getClass.getSimpleName}: ${e.getMessage}
        |${e.getStackTrace.toVector
            .dropWhile(!_.toString.startsWith("aoc"))
            .takeWhile(!_.toString.startsWith("aoc.utils.Problem"))
            .init
            .map(s => s"      $s")
            .mkString("\n")}""".stripMargin

  private val wd = os.pwd / "src" / "main"

  private def readFile(folder: String, year: String, file: String) =
    Try(os.read.lines(wd / "resources" / "input" / folder / year / file).toVector) match
      case Success(lines) => Some(lines)
      case Failure(e) =>
        printlln(s"""|${error(s"when reading $file in $folder/$year")}:
                     |    ${e}""".stripMargin)(using printResult = true)
        None

  val exampleInput = readFile("examples", year, s"$day.txt")
  val puzzleInput = readFile("puzzles", year, s"$day.txt")

  private def solve(data: Option[Vector[String]]): Option[TimedEval[A]] = data.map(d => time(solve(d)))

  /** Attempts to solve the given problem using the given data. 
    * @param printResult Given boolean that decides whether to print the result or not. Defaults to true. 
    * @return The solution if it was found along with the time it took to solve it, or None if no solution was found or if the test case didn't pass.
    */
  def execute(using printResult: Boolean = true) = 
    for i <- 1 to 100 do printlln()
    printlln(info(toString))
    printlln(info("Evaluating example input..."))

    def solvingError(name: String, e: Throwable) = 
      printlln(s"""|${error(s"when solving the $name problem:")}
                   |${tinyStack(e)}""".stripMargin)
      None

    def solvingFail(name: String, eval: TimedEval[A]) = 
      printlln(f"""|${error(s"${RED}Example failed!${RESET}", trim = true)}
                   |    Expected: ${CYAN}${expectedExampleSolution}${RESET}
                   |    Actual:   ${YELLOW}${eval.result}${RESET}
                   |    Time: ${eval.duration}%2.6f s""".stripMargin)
      None

    def solvingSuccess(name: String, eval: TimedEval[A]) =
      printlln(f"""|${success(s"${name.capitalize} input passed!")}
                   |    Output: ${YELLOW}${eval.result}${RESET}
                   |    Time: ${eval.duration}%2.6f s%n""".stripMargin)
      Some(eval)

    val result = Try(solve(exampleInput)) match
      case Failure(e) => solvingError("example", e)
      case Success(None) => None
      case Success(Some(exampleEval)) =>
        if exampleEval.result != expectedExampleSolution 
        then solvingFail("example", exampleEval)
        else
          solvingSuccess("example", exampleEval)
          Try(solve(puzzleInput)) match
            case Failure(e) => solvingError("puzzle", e)
            case Success(None) => None
            case Success(Some(puzzleEval)) => 
              solvingSuccess("puzzle", puzzleEval)

    result match
      case None => 
      case Some(eval) =>
        val date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
        val res = s"$date;$year;$day;$part;${f"${eval.duration}%2.6f"}s;${eval.result};not submitted"
        val file = wd / "resources" / "results.csv"
        val current = os.read.lines(file)
        current
          .tail
          .map(_.split(";").toList)
          .collect { case _ :: `year` :: `day` :: `part` :: _ :: _ :: status :: Nil => status }
          .headOption match
            case Some("submitted") => 
              printlln(info("This solution has been submitted and verified to be correct!"))
            case Some("not submitted") => 
              val m = current.map(s => if s contains s"$year;$day;$part" then res else s).toSeq
              os.write.over(file, m.mkString("", "\n", "\n"))
            case None | Some(_) => os.write.append(file, s"$res\n")

    result

  val result = if exampleInput.isDefined && puzzleInput.isDefined then execute else None
