package aoc.utils
import collection.mutable as mutable

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
  * @param cost the cost of traveling along this edge
  */
case class Edge[V](u: V, v: V, weight: Double):
  val (from, to) = (u, v)
  lazy val reverse = Edge(v, u, weight)
  override def toString = s"$u -> $v @ $weight"

/** 
  * @param vertices the ordered sequence of vertices making up the path
  * @param cost the total cost of the path
  */
case class Path[V](vertices: Seq[V], cost: Double):
  lazy val reverse = Path(vertices.reverse, cost)
  def apply = vertices.apply
  def head = vertices.head
  def last = vertices.last
  override def toString = s"[${vertices.mkString(" -> ")}] @ $cost"

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