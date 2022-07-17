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
  (val example: Example[A]) extends App:

  def name: String
  def solve(data: List[String]): A
  override def toString = s"Day $day: $name ($year)"

  private def printlln(x: Any = "")(using printResult: Boolean) = 
    if printResult then println(x)
  
  private def error(msg: String, trim: Boolean = false) =
    s"[${RED}!${RESET}] ${if !trim then s"${RED}Something went wrong${RESET} " else ""}$msg"

  private def success(msg: String) =
    s"[${GREEN}o${RESET}] ${GREEN}$msg${RESET}"

  private def info(msg: String) =
    s"[${CYAN}+${RESET}] $msg"

  private def tinyStack(e: Throwable) =
    s"""|[${RED}!${RESET}] ${e.getClass.getSimpleName}: ${e.getMessage}
        |${e.getStackTrace.toList
            .dropWhile(!_.toString.startsWith("aoc"))
            .takeWhile(!_.toString.startsWith("aoc.utils.Problem"))
            .init
            .map(s => s"      $s")
            .mkString("\n")}""".stripMargin

  private def solvingError(name: String, exception: Throwable)(using Boolean): Option[TimedEval[A]] = 
    printlln(s"""|${error(s"when solving the $name problem:")}
                 |${tinyStack(exception)}""".stripMargin)
    None

  private def solvingFail(name: String, eval: TimedEval[A])(using Boolean): Option[TimedEval[A]] = 
    printlln(f"""|${error(s"${RED}Example failed!${RESET}", trim = true)}
                 |    Expected: ${CYAN}${example.solution}${RESET}
                 |    Actual:   ${YELLOW}${eval.result}${RESET}
                 |    Time: ${eval.duration}%2.6f s""".stripMargin)
    None

  private def solvingSuccess(name: String, eval: TimedEval[A])(using Boolean): Option[TimedEval[A]] =
    printlln(f"""|${success(s"${name.capitalize} solution found!")}
                 |    Output: ${YELLOW}${eval.result}${RESET}
                 |    Time: ${eval.duration}%2.6f s%n""".stripMargin)
    Some(eval)

  private val resources = os.pwd / "src" / "main" / "resources"

  private def readFile(folder: String, year: String, file: String) =
    Try(os.read.lines(resources / "input" / folder / year / file).toList) match
      case Success(lines) => Some(lines)
      case Failure(e) =>
        printlln(s"""|${error(s"when reading $file in $folder/$year")}:
                     |    ${e}""".stripMargin)(using printResult = true)
        None

  private def writeResult(eval: TimedEval[A])(using Boolean) = 
    val date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
    val res = s"$date;$year;$day;$part;${f"${eval.duration}%2.6f"}s;${eval.result};not submitted"
    val file = resources / "results.csv"
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

  private val primaryExampleInput = readFile("examples", year, s"$day-primary.txt")
  private val secondaryExampleInput = readFile("examples", year, s"$day-secondary.txt")
  private val puzzleInput = readFile("puzzles", year, s"$day.txt")

  private def solve(data: Option[List[String]]): Option[TimedEval[A]] = data.map(d => time(solve(d)))

  /** Attempts to solve the given problem using the given data. 
    * @param printResult Given boolean that decides whether to print the result or not. Defaults to true. 
    * @return The solution (if it was found), along with the time it took to solve it, or None if no solution was found or if the test case didn't pass, given that an example was provided.
    */
  def execute(using printResult: Boolean = true) = 
    for i <- 1 to 100 do printlln()
    printlln(info(toString))

    def solveExample(input: Option[List[String]], solution: A) = 
      Try(solve(input)) match
        case Failure(e) => solvingError("example", e)
        case Success(None) => None
        case Success(Some(exampleEval)) =>
          if exampleEval.result == solution
          then solvingSuccess("example", exampleEval)
          else solvingFail("example", exampleEval)

    def solvePuzzle = Try(solve(puzzleInput)) match
      case Failure(e) => solvingError("puzzle", e)
      case Success(None) => None
      case Success(Some(puzzleEval)) => 
        solvingSuccess("puzzle", puzzleEval)

    val result = example match
      case Skip => 
        printlln(info("No example provided, evaluating puzzle input..."))
        solvePuzzle
      case sol => 
        printlln(info("Evaluating example input..."))
        val (exampleInput, solution) = sol match
          case Primary(solution) => (primaryExampleInput, solution)
          case Secondary(solution) => (secondaryExampleInput, solution)
          case Skip => ??? // should be unreachable
        solveExample(exampleInput, solution).flatMap(_ => 
          printlln(info("Evaluating puzzle input..."))
          solvePuzzle
        )

    result.foreach(writeResult)
    result

  val result = puzzleInput.flatMap(_ => execute)
