package aoc
import math.Numeric.Implicits.infixNumericOps
import math.Integral.Implicits.infixIntegralOps
import math.Fractional.Implicits.infixFractionalOps
import Console.*

package object utils:
  extension [A](a: A)
    /** Logs any object `a` in the console and then returns the object without modifying it. */
    def debug = 
      println(s"[${YELLOW}*${RESET}] $a")
      a

    /** Logs any attribute of object `a` in the console and then returns the object without modifying it. */
    def debugAttr[B](f: A => B) = 
      println(s"[${YELLOW}*${RESET}] ${f(a)}")
      a

    /** Logs any object `a` in the console without the "[*]" prefix and then returns the object without modifying it. */
    def debugClean = 
      println(a)
      a
    
    /** Logs any object `a` in the console *if* the condition `p` is satisfied and then returns the object without modifying it. */
    def warn(p: A => Boolean) = 
      if p(a) then println(s"[${RED}!${RESET}] $a")
      a

    def log(color: String = "cyan") =
      val col = color.toLowerCase match
        case "cyan" => CYAN
        case "red" => RED
        case "green" => GREEN
        case "yellow" => YELLOW
        case "blue" => BLUE
        case "magenta" => MAGENTA
        case _ => CYAN
      println(s"[${col}+${RESET}] $a")
      a
    
  extension [A](xs: IndexedSeq[A])
    /** Zips two sequences and applies a function on the resulting tuples. Functionally equivalent to `.zip(...).map(...)`. */
    def zipWith[B, C](ys: IndexedSeq[B])(f: (A, B) => C) =
      (xs zip ys).map(f.tupled)

  extension [A: Numeric](xs: IndexedSeq[A])
    def average = xs.sum.toDouble / xs.size
    def median(using Ordering[A]) = 
      val sorted = xs.sorted
      val half = xs.size / 2
      if xs.size % 2 == 0 then (sorted(half) + sorted(half - 1)).toDouble / 2
      else sorted(half).toDouble

  extension [A: Numeric](xs: Vector[A])
    def toPos3D = 
      require(xs.size == 3)
      Pos3D(xs(0).toInt, xs(1).toInt, xs(2).toInt)

    /** Computes the [dot product](https://en.wikipedia.org/wiki/Dot_product) of 2 vectors of the same length. */
    infix def dot (ys: Vector[A]) = 
      require(xs.size == ys.size, "Vectors must be the same size")
      (xs zip ys map (_ * _)).sum

    /** Computes the [cross product](https://en.wikipedia.org/wiki/Cross_product) of 2 vectors of length 3. */
    infix def cross (ys: Vector[A]) = 
      require(xs.size == 3 && ys.size == 3, "Cross product only defined for 3D vectors")
      Vector(
        xs(1) * ys(2) - xs(2) * ys(1), 
        xs(2) * ys(0) - xs(0) * ys(2), 
        xs(0) * ys(1) - xs(1) * ys(0)
      )

    /** Returns the [magnitude](https://en.wikipedia.org/wiki/Magnitude_(mathematics)#Vector_spaces) of the vector. */
    def magnitude = math.sqrt((xs dot xs).toDouble)

    /** Returns a [normalized](https://en.wikipedia.org/wiki/Unit_vector) version of the vector. */
    def normalized = xs.map(_.toDouble * (1.0 / xs.magnitude))

  /** Represents a position in 2D space */
  case class Pos(x: Int, y: Int):
    /** Switches the x and y coordinates */
    def transpose = Pos(y, x)
    def tuple = (x, y)
    
    def +(p: Pos) = Pos(x + p.x, y + p.y)
    def -(p: Pos) = Pos(x - p.x, y - p.y)

    def distance(p: Pos) = math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y))
    def manhattan(p: Pos) = math.abs(x - p.x) + math.abs(y - p.y)
    
  /** Represents a position in 3D space */
  case class Pos3D(x: Int, y: Int, z: Int):
    def tuple = (x, y, z)
    def toVector = Vector(x, y, z)
    
    def +(p: Pos3D) = Pos3D(x + p.x, y + p.y, z + p.z)
    def -(p: Pos3D) = Pos3D(x - p.x, y - p.y, z - p.z)
    
    def distance(p: Pos3D) = 
      math.sqrt((x - p.x).toDouble * (x - p.x) + (y - p.y).toDouble * (y - p.y) + (z - p.z).toDouble * (z - p.z))
    def manhattan(p: Pos3D) = 
      math.abs(x - p.x) + math.abs(y - p.y) + math.abs(z - p.z)

  case class Line(start: Pos, end: Pos)
  case class Line3D(start: Pos3D, end: Pos3D)

  /** A simple wrapper class that includes the result of an evaluation and the time it took to execute it
   * @param result The final evaluation
   * @param time Time elapsed while evaluating, in seconds
   */
  case class TimedEval[A](duration: Double, result: A)
  object TimedEval:
    /** Times the evaluation of a block of code */
    def time[A](block: => A): TimedEval[A] =
      val start = System.nanoTime()
      val result = block
      val duration = (System.nanoTime() - start) / 1E9
      TimedEval(duration, result)

  /** Experimental features. Not fully tested. */
  object Experimental:
    /** Simple wrapper case class that holds a result of an evaulation and the the amount of iterations it took to get there. */
    case class Counter[A](value: A, count: Int)

    extension [A] (a: A)
      /** Uses a [[scala.collection.immutable.LazyList]] to apply any function `A => A` on any object `n` times. Easily curry-able due to usage of multiple parameter lists. */
      def iterate[B](f: A => A)(n: Int): A = 
        LazyList.iterate(a)(f)(n)
      
      /** Recursively applies a function `f: A => A` on any object `a` until `f(a)` is equal to `a`.
       */
      def finalize[B](f: A => A): A =
        if a == f(a) then a
        else a finalize f

      /** Recursively applies a function `f: A => A` on any object `a` until `f(a)` is equal to `a`.
        * @return the result of the repeated function on `a`.
        * @return the number of times the function was applied.
      */
      def finalizeCount[B](f: A => A, count: Int = 0): Counter[A] =
        if a == f(a) then Counter(a, count)
        else a finalizeCount (f, count + 1)

    object IterableExtensions:
      // Doesn't really work how I want it to;
      // It still returns a list of the same subtype as called 
      // on, but the super type gets abstracted up to Iterable,
      // which isn't really optimal.
      extension [A] (xs: collection.Iterable[A])
        def exists(f: (A, Int) => Boolean) = xs.zipWithIndex.exists(f.tupled)
        def forall(f: (A, Int) => Boolean) = xs.zipWithIndex.forall(f.tupled)
        def foreach(f: (A, Int) => Unit) = xs.zipWithIndex.foreach(f.tupled)
        def find(f: (A, Int) => Boolean) = xs.zipWithIndex.find(f.tupled).map(_._1)
        def findIndex(f: (A, Int) => Boolean) = xs.zipWithIndex.find(f.tupled).map(_._2)
        def map[B](f: (A, Int) => B) = xs.zipWithIndex.map(f.tupled)
        def filter(f: (A, Int) => Boolean) = xs.zipWithIndex.filter(f.tupled).map(_._1)
        def filterNot(f: (A, Int) => Boolean) = xs.zipWithIndex.filterNot(f.tupled).map(_._1)