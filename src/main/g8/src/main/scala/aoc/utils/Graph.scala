package aoc.utils
import scala.collection.mutable as mutable

/** 
  * @param from the source vertex
  * @param to the destination vertex
  * @param cost the cost of traveling along this edge
  */
case class Edge[V](u: V, v: V, weight: Double):
  val (from, to) = (u, v)
  lazy val reverse = Edge(v, u, weight)
  override  def toString = s"$u -> $v @ $weight"


/** 
  * @param vertices the ordered sequence of vertices making up the path
  * @param cost the total cost of the path
  */
case class Path[V](vertices: Seq[V], cost: Double):
  lazy val reverse = Path(vertices.reverse, cost)
  def ++(other: Path[V]) = Path(vertices ++ other.vertices, cost + other.cost)
  override def toString = s"[${vertices.mkString(" -> ")}] @ $cost"


case class DisjointSet[A](nodes: A*):
  import collection.mutable as mutable
  val parents = mutable.Map.empty[A, A]
  val rank = mutable.Map.empty[A, Int]

  for node <- nodes do
    parents(node) = node
    rank(node) = 0

  def findSet(v: A): A =
    if parents(v) != v then parents(v) = findSet(parents(v))
    parents(v)

  def makeUnion(a: A, b: A) =
    val u = findSet(a)
    val v = findSet(b)
    if u != v then rank(u) compare rank(v) match
      case 1  => parents(v) = u
      case -1 => parents(u) = v
      case 0  => 
        parents(u) = v
        rank(v) += 1

  def add(v: A) =
    parents(v) = v
    rank(v) = 0

object DisjointSet:
  def apply[A](nodes: Iterable[A]): DisjointSet[A] = DisjointSet(nodes.toSeq*)


/** A data structure for representing a directed or undirected graph, with some related functions.
  * @tparam V the type of the vertices in the graph
  * @param vertices a set of all vertices in the graph
  * @param edges a set of all edges in the graph
  * @param edgesFrom a map from each vertex to a set of edges leaving it
  * @param edgesTo a map from each vertex to a set of edges entering it
  * @param adjacent a map from each vertex to a set of vertices adjacent to it
  */
case class Graph[V](edges: Set[Edge[V]]):
  val size = edges.size
  lazy val vertices = edges.flatMap(e => Seq(e.u, e.v))
  lazy val edgesFrom = edges.groupBy(_.from)
  lazy val edgesTo = edges.groupBy(_.to)
  lazy val adjacent = edgesFrom
    .zip(edgesTo)
    .collect{ case ((a, s1), (b, s2)) if a == b => (a, s1 ++ s2) }
    .toMap

  /** Calculates the mimimum spanning tree of this graph using [Krukal's algorithm](http://en.wikipedia.org/wiki/Kruskal%27s_algorithm). Assumes the graph is undirected.
  * @return `Some(mst)` if the graph is fully connected, `None` otherwise
  */
  def minimumSpanningTree = 
    val queue = mutable.PriorityQueue.empty[Edge[V]](Ordering.by(_.weight)).reverse
    val mst = mutable.Set.empty[Edge[V]]
    val djs = DisjointSet(vertices)
    queue ++= edges

    while queue.nonEmpty do
      val edge = queue.dequeue
      if djs.findSet(edge.u) != djs.findSet(edge.v) then
        mst += edge
        djs.makeUnion(edge.u, edge.v)

    if mst.size != vertices.size - 1 then Some(mst.toSet) else None

  /** Finds the shortest path from the given starting vertex to the destination vertex in the graph.
  * Uses [Dijkstra's algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm), with an optional heuristic method parameter to make it [A*](https://en.wikipedia.org/wiki/A*_search_algorithm). Assumes the graph is directed, and that there are no negative weights in any of the edges.
  * @param start the starting vertex.
  * @param end the goal vertex.
  * @param heuristic an optional heuristic function `V => Double`
  * @return `Some[Path]` if such a path exists, `None` otherwise.
  */
  def shortestPath(start: V, end: V, heuristic: V => Double = (v: V) => 0) =
    val pq = mutable.PriorityQueue((start, 0d))(Ordering.by((a, b) => -b))
    val prev = mutable.Map.empty[V, V]
    val dist = mutable.Map.empty[V, Double] withDefaultValue Double.PositiveInfinity
    dist(start) = 0

    var found = false
    while !found && pq.nonEmpty do
      val (min, _) = pq.dequeue

      if min == end then found = true
      else for edge <- edgesFrom getOrElse (min, Set.empty) do
        val alt = dist(min) + edge.weight
        val dest = edge.to
        if alt < dist(dest) then
          pq.enqueue((dest, alt + heuristic(dest)))
          dist(dest) = alt
          prev(dest) = min

    def backtrack(current: V): Vector[V] =
      if prev(current) == start then Vector(start, current)
      else backtrack(prev(current)) :+ current

    if found then Some(Path(backtrack(end), dist(end))) 
    else None