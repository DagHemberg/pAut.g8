package aoc.utils
import scala.collection.mutable as mutable

// TODO
// - components: Set[Graph[V]]
// - stronglyConnectedComponents: Set[Graph[V]]

trait Graph[V]:
  import Graph.*
  protected given adjacenctTo: (V => Set[Edge[V]])

  def apply(v: V) = adjacenctTo(v)
  def apply(a: V)(b: V) = adjacenctTo(a).find(_.to == b).map(_.weight)

  def vertices: Set[V]
  def edges: Set[Edge[V]]
  def edgesFrom: Map[V, Set[Edge[V]]]
  def edgesTo: Map[V, Set[Edge[V]]]

  def pathsFrom(start: V) = dijkstra(start)
  def pathBetween(start: V, end: V, heuristic: V => Double = _ => 0) = aStar(start, end, heuristic)

  def reachableFrom(start: V) =     
    adjacenctTo(start)
      .map(_.to)
      .converge(s => s ++ s.flatMap(adjacenctTo).map(_.to)) 
      + start

object Graph:
  def apply[V](adjacencyFunction: V => Set[Edge[V]]) = LazyGraph(adjacencyFunction)
  def apply[V](edges: Set[Edge[V]]) = FiniteGraph(edges)
  def apply[V](elems: Edge[V]*) = FiniteGraph(elems.toSet)

  private def backtrack[V](current: V)(using prev: Map[V, V], start: V): Vector[V] =
    if prev(current) == start then Vector(start, current)
    else backtrack(prev(current)) :+ current

  private def fastBfs[V]
    (start: V, stoppingPredicate: V => Boolean, heuristic: V => Double)
    (using adj: V => Set[Edge[V]]) = 
    
    val pq = mutable.PriorityQueue(start -> 0d)(Ordering.by((a, b) => -b))
    val prev = mutable.Map.empty[V, V]
    val dist = mutable.Map(start -> 0d) withDefaultValue Double.PositiveInfinity

    var found = false
    while !found && pq.nonEmpty do
      val (min, _) = pq.dequeue

      if stoppingPredicate(min) then found = true
      else for edge <- adj(min) do
        val alt = dist(min) + edge.weight
        val dest = edge.to
        if alt < dist(dest) then
          pq.enqueue((dest, alt + heuristic(dest)))
          dist(dest) = alt
          prev(dest) = min

    (dist.toMap, prev.toMap, found)

  def dijkstra[V](start: V)(using V => Set[Edge[V]]): Set[Path[V]] = 
    val (dist, prev, _) = fastBfs(start, _ => false, _ => 0)
    (dist.keySet - start).map(v => Path(backtrack(v)(using prev, start), dist(v))) + Path(Seq(start), 0)

  def aStar[V](start: V, end: V, heuristic: V => Double)(using V => Set[Edge[V]]) = 
    val (dist, prev, found) = fastBfs(start, _ == end, heuristic)
    if found then Some(Path(backtrack(end)(using prev, start), dist(end)))
    else None

case class LazyGraph[V](adjacencyFunction: V => Set[Edge[V]]) extends Graph[V]:
  override def toString = if edges.isEmpty 
    then "LazyGraph(<not computed>)"
    else s"LazyGraph(${edges.mkString("", ", ", ", ")}<not computed>)"

  private val mVertices = mutable.Set.empty[V]
  private val mEdges = mutable.Set.empty[Edge[V]]
  private val mEdgesFrom = mutable.Map.empty[V, Set[Edge[V]]] withDefaultValue Set.empty
  private val mEdgesTo = mutable.Map.empty[V, Set[Edge[V]]] withDefaultValue Set.empty

  def vertices = mVertices.toSet
  def edges = mEdges.toSet
  def edgesFrom = mEdgesFrom.toMap 
  def edgesTo = mEdgesTo.toMap

  protected final given adjacenctTo: (V => Set[Edge[V]]) = v => 
    val edgs = mEdgesFrom.getOrElseUpdate(v, adjacencyFunction(v))
    mEdgesTo ++= edgs.groupBy(_.to)
    mVertices ++= edgs.flatMap(e => Set(e.u, e.v))
    mEdges ++= edgs
    edgs

case class FiniteGraph[V](private val elems: Set[Edge[V]]) extends Graph[V]:
  override def toString = s"Graph(${elems.mkString(", ")})"
  val edges = elems
  lazy val vertices = elems.flatMap(e => Set(e.u, e.v))
  lazy val edgesFrom = edges.groupBy(_.from) withDefaultValue Set.empty
  lazy val edgesTo = edges.groupBy(_.to) withDefaultValue Set.empty

  protected final given adjacenctTo: (V => Set[Edge[V]]) = edgesFrom.apply  

  def transpose = Graph(edges.map(_.reverse))
