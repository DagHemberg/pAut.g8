package aoc.utils
import collection.mutable as mutable

// not sure if i really need this?
// could be nice to have eventually but might move to utils package object
case class Tree[V](value: V, children: Set[Tree[V]] = Set.empty[Tree[V]]):
  override def toString = 
    if children.isEmpty then value.toString
    else s"{ $value -> { ${children.mkString(", ")} } }"

  def isSubtreeOf(other: Tree[V]): Boolean =
    this == other || other.children.exists(this.isSubtreeOf)

  lazy val vertices: Set[V] = children.flatMap(_.vertices) + value

/** 
  * @param from the source vertex
  * @param to the destination vertex
  * @param weight the cost of traveling along this edge
  */
case class Edge[V](from: V, to: V, weight: Double = 1):
  val (u, v) = (from, to)
  lazy val reverse = Edge(to, from, weight)
  override def toString = s"Edge($from -> $to, $weight)"

/** 
  * @param vertices the ordered sequence of vertices making up the path
  * @param cost the total cost of the path
  */
case class Path[V](vertices: Seq[V], cost: Double):
  lazy val reverse = Path(vertices.reverse, cost)
  def apply = vertices.apply
  def head = vertices.head
  def last = vertices.last
  override def toString = s"Path(${vertices.mkString(" -> ")}, $cost)"

case class DisjointSets[V](nodes: V*):
  override def toString = parents.groupBy(_._2).map(_._2).toSet.mkString("[", ", ", "]")
  val parents = mutable.Map.empty[V, V]
  val rank = mutable.Map.empty[V, Int]

  for node <- nodes do
    parents(node) = node
    rank(node) = 0

  def findSet(v: V): V =
    if parents(v) != v then parents(v) = findSet(parents(v))
    parents(v)

  def makeUnion(a: V, b: V) =
    val u = findSet(a)
    val v = findSet(b)
    if u != v then rank(u) compare rank(v) match
      case 1  => parents(v) = u
      case -1 => parents(u) = v
      case 0  => 
        parents(u) = v
        rank(v) += 1

  def add(v: V) =
    parents(v) = v
    rank(v) = 0

object DisjointSets:
  def apply[V](nodes: Iterable[V]): DisjointSets[V] = DisjointSets(nodes.toSeq*)