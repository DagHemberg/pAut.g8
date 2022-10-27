# pAut

pAut (short for **p**roblem **aut**omation) is a collection of libraries made for streamlining the process of solving problems in [Advent of Code](https://adventofcode.com).

- [sbt-pAut](https://github.com/DagHemberg/sbt-pAut)
- [pAut-program](https://github.com/DagHemberg/pAut-program)
- [ProblemUtils](https://github.com/DagHemberg/ProblemUtils)

## Quickstart

Make sure you have sbt installed, and run

```text
sbt new DagHemberg/pAut.g8
```

in your shell of choice. This will create a new folder in your current directory with the necessary library and plugin dependencies required to run the program.

## Manual installation

If you instead want to add all dependencies manually, start by adding

```sbt
addSbtPlugin("io.github.daghemberg" % "sbt-paut" % "0.1.6")
```

to the `plugins.sbt` file in your `project` folder. (If the file doesn't exist, you can add it manually.)

Keep in mind that files created by this assume that you're also using the pAut-problem and ProblemUtils libraries, so make sure you also add

```sbt
libraryDependencies ++= Seq(
  "io.github.daghemberg" %% "paut-program" % "0.1.4",
  "io.github.daghemberg" %% "problemutils" % "0.1.1"
)
```

to your `build.sbt`.

## Usage

When you've initialized the project, `cd` into it and type `sbt` to start the sbt console. The sbt-pAut plugin adds a new command to sbt, `aoc`, which contains multiple subcommands.

`aoc <help | auth | data | results>`

Each of these subcommands also contain their own subcommands -- for a more thorough explanation of what each command does, type `aoc help` and hit the Tab key to see what commands are available.

### Authenticating

In order to initialize problem files and submit results, you need to authenticate with the Advent of Code website. This is done by retrieving the session token and running the `aoc auth set <token>` command.

1. Login to [Advent of Code](https://adventofcode.com).
2. Open the developer console.
   - This is usually done by pressing `F12` or `Ctrl+Shift+I` -- on Safari, this isn't enabled by default. Follow [this guide](https://support.apple.com/guide/safari/sfri20948/mac) to enable the Developer tools tab and then either press `Develop > Show Web Inspector` or hit `⌥⌘I`.
3. Open the Network tab and refresh the page.
4. Click on the request labeled either `adventofcode.com` or a year (e.g `2022`, `2021`, etc).
5. Copy the value of the cookie named `session`.
6. Use the copied value in the `aoc auth` command, i.e `aoc auth set reallylongsessioncookievalue0123456789876543210`.

### Initializing problems

The command for initializing a new problem is structured as follows: `aoc data initProblem "<name>" <day> [year]` -- so if you wanted to solve the problem(s) from December 13, 2017, you'd run `aoc data initProblem "Packet Scanners" 13 2017`. This would create a folder under `./src/main/scala/aoc` named `13-packet-scanners` containing 4 files:

- 2 "problem files" (`Part1.scala` and `Part2.scala`)
- A `package.scala` meant to be used for any code shared between the 2 parts
- A `testing.worksheet.sc` as a sort of coding whiteboard where you can try out ideas and have them immediately evaluated.

The command will also automatically download and cache the input data for the day in question -- but if something goes wrong during this process, you can manually download the correct data yourself using the `aoc data fetchManual` command.

#### Example input file

When running `initProblem` commmand, a new file will be opened in your default text editor telling you to "Paste your example data here!". Problems in Advent of Code often have smaller "sample" inputs, along with the solution to the problem, given that input data. For December 13, 2017, that sample data was

```text
0: 3
1: 2
4: 4
6: 4
```

meaning that's what you should paste in the example file (removing what's already there).

If a problem doesn't have an example, or has different examples for the different parts, see the sections on [Automatic testing](#automatic-testing) and/or [Multiple example files](#multiple-example-files).

### Solving problems

When running the command mentioned in the above section, a file containing the following code would be produced

```scala
// imports

object Part1 extends Problem(13, 2017)(1)(???):
  def name = "Packet Scanners - Part 1"
  def solve(data: List[String]) = ???
```

The Problem class extends `App`, meaning each Part object becomes its own runnable program -- simply type `run` in the sbt console and select which problem to solve.

`(13, 2017)(1)` represents the day, year and part of the problem which the program is attempting to solve -- so the example above would represent the first part of the problem published on December 13, 2017.

Following those is `(???)`, which is the expected solution to the example input data. The question marks are just a placeholder, meaning you have to replace them yourself with the expected solution (given the example input) for the problem in question. For Decemer 13 part 1, 2017, this solution is `24`, and so is what you'd put in place of the question marks. This argument is also generic, meaning you can have `Int`s, `Double`s, `String`s, or even your own class as your expected output, depending on what the problem is asking of you.

The `solve` method is what actually gets evaluated when you run the program. The input data is given as a list of strings, and it's up to you to determine what to do with it to solve the problem.

#### Automatic testing

`solve` is used twice when running the program -- once for the example data, and once, if the solution given by `solve` matches the expected solution, for the actual input data.

```text
[+] Day 1: Some problem - Part 1 (2022)
[+] Evaluating example input...
[o] Example solution found!
    Output: Solution for example input
    Time: 0.000123 s

[+] Evaluating puzzle input...
[o] Puzzle solution found!
    Output: Solution for actual input
    Time: 0.000456 s
```

(Keep in mind though, just because the example input passes doesn't mean the solution to the actual input is necessarily correct!)

If the solution given doesn't match, you'll get an error message and the actual data won't be evaluated:

```text
[+] Day 1: Some problem - Part 1 (2022)
[+] Evaluating example input...
[!] Example failed!
    Expected: Some solution
    Got:      Another solution
    Time: 0.000069 s
```

Sometimes, though, a problem doesn't have an example. If this happens, simply put `Skip` as your expected example solution:

```scala
object PartN extends Problem(day, year)(part)(Skip):
  // etc
```

This will skip the evaluation of the example data and go straight to evaluating the actual data.

```text
[+] Day 1: Some problem - Part 1 (2022)
[+] No example provided, skipping...
[+] Evaluating puzzle input...
```

#### Multiple example files

If the second part of a problem contains some other example input data than the first part, you can specify which example file to read from by adding an integer argument to the third parameter list.

```text
aoc data addExample <day> [year]
```

```scala
object Part2 extends Problem(day, year)(2)("some other answer", i = 2):
  // etc
```

### Viewing and submitting results

Say you've found a passing result using the example data and found a result which you think is correct for the actual data. You can view a more detailed view of the result using `aoc results get <part> <day> [year]`, or a comprehensive list of all results with `aoc results viewAll`.

To submit a result to the Advent of Code website, run `aoc results submit <part> <day> [year]`, which will verify the validity of your result. Once submitted, the local result is fixed, and the only changes that can be made are the time (optimizations only) and the timestamp of the last run.

### Default year

In all cases when inputting a date in the `aoc` command, the `[year]` argument is optional and defaults to the latest year with available problems. It's also updated dynamically, meaning when a new year of Advent of Code starts the default year will update to reflect this.

You can, of course, also manually set the default year using `aoc defaultYear set <year>`. Doing this will also prevent the default year from updating dynamically.

#### The 'today' keyword

If you're actively following and solving new problems as they're being released, you can use the `today` keyword anywhere a date is being asked for in the `aoc` command, given that a problem was actually published that day. For example:

```text
aoc data initProblem "Just some name" today
aoc data fetchManually today
aoc results submit 2 today
```

Note: new problems are published 00:00 / 12:00AM EST
