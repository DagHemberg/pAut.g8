package aoc.utils
import math.Numeric.Implicits.infixNumericOps
import math.*

/** A helper class for [[aoc.utils.Matrix]] used for indexing. */
case class Index(row: Int, col: Int):
  override def toString: String = s"($row, $col)"
  lazy val up = Index(row - 1, col)
  lazy val down = Index(row + 1, col)
  lazy val left = Index(row, col - 1)
  lazy val right = Index(row, col + 1)
  lazy val nw = Index(row - 1, col - 1)
  lazy val ne = Index(row - 1, col + 1)
  lazy val sw = Index(row + 1, col - 1)
  lazy val se = Index(row + 1, col + 1)

  lazy val neighbors = List(up, down, left, right, nw, ne, sw, se)
  lazy val neighboursOrth = List(up, left, right, down)
  lazy val neighboursDiag = List(nw, ne, sw, se)

  def neighborsIn[A](using mat: Matrix[A]) = neighbors.filterNot(mat.indexOutsideBounds).map(mat.apply)
  def neighborsOrthIn[A](using mat: Matrix[A]) = neighboursOrth.filterNot(mat.indexOutsideBounds).map(mat.apply)  
  def neighboursDiagIn[A](using mat: Matrix[A]) = neighboursDiag.filterNot(mat.indexOutsideBounds).map(mat.apply)

/** A generic Matrix class. Useful for working with 2D structures.
 * @tparam A The type of elements in the matrix. When `A` is a [[scala.Numeric]] type, a number of extension methods are made available which allow for basic mathematical matrix operations.
 * @param rows The number of rows in the matrix.
 * @param cols The number of columns in the matrix.
 * @param size A tuple of the height and width of the matrix.
*/
case class Matrix[A](input: Vector[Vector[A]]):
  require(input.forall(_.size == input.head.size), "All rows must have the same length")
  require(input.size > 0, "Matrix must have at least one row")
  require(input.head.size > 0, "Matrix must have at least one column")

  lazy val height = input.size
  lazy val width = input.head.size
  lazy val size = (height, width)

  // doesn't work super well with other multi-line toStrings 
  override def toString = 
    if input.size == 1 then input.head.mkString("( ", " ", " )")
    else  
      def pad(vec: Vector[A]) = vec
        .zipWith
          (input.transpose.map(_.map(_.toString.size).max + 1))
          ((a, b) => a.toString.reverse.padTo(b, ' ').reverse)
        .mkString
      
      s"\n⎛${pad(input.head)} ⎞${if input.size == 2 then "" else s"\n${input.init.tail.map(row => s"⎜${pad(row)} ⎟").mkString("\n")}"}\n⎝${pad(input.last)} ⎠"

  def apply(row: Int, col: Int): A = input(row)(col)
  def apply(index: Index): A = input(index.row)(index.col)

  def row(row: Int) = input(row).toVector
  def col(col: Int) = input.map(_(col)).toVector
  
  def toVector = input.map(_.toVector).toVector
  def toVectors = toVector
  def rows = toVector
  def cols = toVector.transpose

  def indices: Matrix[Index] = 
    (0 until height).toVector.map(row => (0 until width).toVector.map(col => Index(row, col))).toMatrix

  def indexOutsideBounds(row: Int, col: Int): Boolean =
    row < 0 || row >= height || col < 0 || col >= width
  def indexOutsideBounds(index: Index): Boolean = 
    indexOutsideBounds(index.row, index.col)

  def map[B](f: A => B) = input.map(_.map(f)).toMatrix
  def forEach(f: A => Unit) = input.foreach(_.foreach(f))
  def count(f: A => Boolean) = input.map(_.count(f)).sum
  def forall(f: A => Boolean) = input.forall(_.forall(f))
  def exists(f: A => Boolean) = input.exists(_.exists(f))

  def slice(row: Int, col: Int)(width: Int, height: Int): Matrix[A] = 
    input.slice(row, row + width).map(_.slice(col, col + height)).toMatrix
  def slice(index: Index)(width: Int, height: Int): Matrix[A] = 
    slice(index.row, index.col)(width, height)

  def filterRow(f: Vector[A] => Boolean) = input.filter(f).toMatrix
  def filterCol(f: Vector[A] => Boolean) = input.transpose.filter(f).transpose.toMatrix

  def transpose = input.transpose.toMatrix // ??? since when is this a thing
  def flipCols = input.map(_.reverse).toMatrix
  def flipRows = input.reverse.toMatrix

  def swapRows(a: Int, b: Int) = 
    input.updated(a, input(b)).updated(b, input(a)).toMatrix
  def swapCols(a: Int, b: Int) =
    transpose.swapRows(a, b).transpose

  def appendedLeft(other: Matrix[A]): Matrix[A] = 
    require(other.height == height, "Can't append matrices of different heights to the side")
    Matrix(input.zip(other.input).map((row, otherRow) => otherRow ++ row))

  def appendedRight(other: Matrix[A]): Matrix[A] =
    require(other.height == height, "Can't append matrices of different heights to the side")
    Matrix(input.zip(other.input).map((row, otherRow) => row ++ otherRow))

  def appendedTop(other: Matrix[A]): Matrix[A] =
    require(other.width == width, "Can't append matrices of different widths to the top")
    Matrix(other.input ++ input)

  def appendedBottom(other: Matrix[A]): Matrix[A] =
    require(other.width == width, "Can't append matrices of different widths to the bottom")
    Matrix(input ++ other.input)

  def dropRow(row: Int) = (input.take(row) ++ input.drop(row + 1)).toMatrix
  def dropCol(col: Int) = input.map(row => row.take(col) ++ row.drop(col + 1)).toMatrix

  def zip[B](other: Matrix[B]): Matrix[(A, B)] =
    require(size == other.size, "Can't zip matrices of different dimensions")
    Matrix(input.zip(other.input).map((row, otherRow) => row.zip(otherRow)))

  def zipWithIndex: Matrix[(A, Index)] = this zip indices

  def zipWith[B, C](other: Matrix[B])(f: (A, B) => C): Matrix[C] =
    (this zip other).map(f.tupled)

object Matrix:
  def apply[A](height: Int, width: Int)(f: (Int, Int) => A): Matrix[A] = 
    Matrix((0 until height).toVector.map(row => (0 until width).toVector.map(col => f(row, col))))
  
  def apply[A](height: Int, width: Int)(f: Index => A): Matrix[A] = 
    Matrix(height, width)((r, c) => f(Index(r, c)))

  def identity(size: Int): Matrix[Int] = 
    Matrix(size, size)((row, col) => if row == col then 1 else 0)

  def fill[A](height: Int, width: Int)(value: A): Matrix[A] = 
    Matrix(height, width)(_ => value)

  def rotate2D(rad: Double) = 
    Vector(
      Vector(cos(rad), -sin(rad)), 
      Vector(sin(rad), cos(rad))
    ).toMatrix

  def rotate3DX(rad: Double) = 
    Vector(
      Vector(1, 0, 0), 
      Vector(0, cos(rad), -sin(rad)), 
      Vector(0, sin(rad), cos(rad))
    ).toMatrix

  def rotate3DY(rad: Double) = 
    Vector(
      Vector(cos(rad), 0, sin(rad)), 
      Vector(0, 1, 0), 
      Vector(-sin(rad), 0, cos(rad))
    ).toMatrix

  def rotate3DZ(rad: Double) = 
    Vector(
      Vector(cos(rad), -sin(rad), 0), 
      Vector(sin(rad), cos(rad), 0), 
      Vector(0, 0, 1)
    ).toMatrix

extension [A](vss: Vector[Vector[A]]) 
  def toMatrix: Matrix[A] = Matrix(vss)

extension [A: Numeric](xs: Vector[A])
  def *(mat: Matrix[A]): Vector[A] = (Vector(xs).transpose.toMatrix * mat).toVector.flatten

extension [A: Numeric](mat: Matrix[A])
  def sum = mat.toVector.flatten.sum
  def product = mat.toVector.flatten.product
  def +(other: Matrix[A]) = mat zip other map ((a, b) => a + b)
  def -(other: Matrix[A]) = mat zip other map ((a, b) => a - b)
  def *(other: Matrix[A]): Matrix[A] = 
    require(mat.width == other.height)
    Matrix(mat.height, other.width)((r, c) => mat.row(r) dot other.col(c))

  def *(vec: Vector[A]): Vector[A] = (mat * Vector(vec).transpose.toMatrix).toVector.flatten

  def determinant: A =     
    require(mat.width == mat.height)
    mat.width match
      case 1 => mat(0, 0)
      case 2 => (mat.row(0)(0) * mat.row(1)(1) - mat.row(0)(1) * mat.row(1)(0))
      case n => (0 until n)
        .map(i => (if i % 2 == 0 then mat.col(i)(0) else -mat.col(i)(0)) * mat.dropCol(i).dropRow(0).determinant)
        .sum

extension [A](mat: Matrix[Matrix[A]])
  def flatten = mat.toVector
    .map(_.reduce((acc, curr) => acc.appendedRight(curr)))
    .reduce((acc, curr) => acc.appendedBottom(curr))