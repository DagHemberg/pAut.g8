package aoc.utils

// currently not used

object ProblemList:
  val list = collection.mutable.ListBuffer.empty[Problem[?]]
  def addProblems = 
    