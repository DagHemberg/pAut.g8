# AdventAutomation

A small library for handling manual tasks in [Advent of Code](https://adventofcode.com) a bit more streamlined, right within SBT.

You can view the code docs [here](https://daghemberg.github.io/AdventAutomation).

## Program Usage

Use `run` to get a list of the created problems.

### Commands / Tasks

- run
- init
- auth
- fetch
- year
- submit

### Run

Usage:
`run`

Runs the Scala project. Since all initialized problems are extensions of the `Problem` class, which in turn extends `App`, sbt will bring up a list of all problems and you can select which one to run from there.

#### Init

Usage:  
**`init <day> [name]`**

Initializes parts 1 and 2, a common package file, and a worksheet file, for the given day.

- The `day` argument has to be between 1 and 25.
- The `name` argument is optional and defaults to `"N/A"`.

#### Auth

Usage:
**`auth set <sessionCookie>`**

Sets the authetication token used for communicating with AoC. **Required** for usage of `submit` or `fetch`.  
The session token can be retrieved by following these steps ([credit](https://golangrepo.com/repo/Zyian-aocget))

- Login to Advent of Code
- Open your browser's DevTools (usually `F12` or `Ctrl+Shift+I`)
- Head over to your cookies
- Copy the value of the session cookie

**`auth [get | status]`**

Displays the current token and username.

**`auth retry`**

Re-attempts authentication using the current session token. Useful if submitting or fetching fails and you're not sure why (the tokens expire after about a month, so you should be good to go if you set the token near the start of the month).

#### Fetch

Usage:  
**`fetch <day>`**

Fetches the input data for the given day in the currently set year. Also opens the example input file for the given day in your default text editor (currently untested on Mac and Linux, PRs welcome)

- The `day` argument has to be between 1 and 25.

#### Year

Usage:
**`year set <year>`**

Sets the current year. Used in `fetch`, `init` and `submit`.

**`year get`**

Retrieves the current year.

#### Submit

Usage:
**`submit <day> <part> [year]`**

Attempts to submit the latest successful solution to given problem to AoC. Gives feedback on the result similar to the actual website (e.g "Wait x seconds to submit again", "The answer was too high", etc). Won't submit again if the correct answer has already been submitted.

- The `year` argument is optional, and defaults to the currently set year.
