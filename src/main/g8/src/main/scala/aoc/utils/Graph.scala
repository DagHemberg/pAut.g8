package aoc.utils
import scala.collection.mutable as mutable

// TODO
// - components: Set[Tree[V]]
// - scc: Set[Set[V]]
// - treesFrom(v: V*): List[Tree[V]]

trait Graph[V]:
  import Graph.*
  protected given adjacencyFunction: (V => Set[Edge[V]])

  def paths(start: V) = 
    dijkstra(start)
  def path(start: V, end: V, heuristic: V => Double = _ => 0) = 
    aStar(start, end, heuristic)
  def reachableFrom(start: V) =     
    adjacencyFunction(start)
      .map(_.to)
      .converge(s => s ++ s.flatMap(adjacencyFunction).map(_.to)) 
      + start

object Graph:
  def apply[V](adjacencyFunction: V => Set[Edge[V]]) = LazyGraph(adjacencyFunction)
  def apply[V](edges: Set[Edge[V]]) = FiniteGraph(edges)

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
    dist.keySet.map(v => Path(backtrack(v)(using prev, start), dist(v)))

  def aStar[V](start: V, end: V, heuristic: V => Double)(using V => Set[Edge[V]]) = 
    val (dist, prev, found) = fastBfs(start, _ == end, heuristic)
    if found then Some(Path(backtrack(end)(using prev, start), dist(end)))
    else None

case class LazyGraph[V](adjacency: V => Set[Edge[V]]) extends Graph[V]:
  protected final given adjacencyFunction: (V => Set[Edge[V]]) = adjacency

case class FiniteGraph[V](edges: Set[Edge[V]]) extends Graph[V]:
  protected final given adjacencyFunction: (V => Set[Edge[V]]) = edgesFrom.apply  

  lazy val edgesFrom = edges.groupBy(_.from) withDefaultValue Set.empty
  lazy val edgesTo = edges.groupBy(_.to) withDefaultValue Set.empty
  def transpose = FiniteGraph(edges.map(_.reverse))
