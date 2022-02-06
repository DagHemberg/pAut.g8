package aoc.utils

type Cost = Double
object Cost:
  def MaxValue: Cost = Double.MaxValue

/** 
  * @param from the source vertex
  * @param to the destination vertex
  * @param cost the cost of traveling along this edge
  */
case class Edge[V](from: V, to: V, cost: Cost):
  override  def toString = s"$from -> $to @ $cost"

/** 
  * @param vertices the ordered sequence of vertices making up the path
  * @param cost the total cost of the path
  */
case class Path[V](vertices: Seq[V], cost: Cost):
  override def toString = s"[${vertices.mkString(" -> ")}] @ $cost"

/** A utility class for calculating the shortest path between two generic vertices in a graph. 
  * Uses the Dijkstra algorithm, with an optional heuristic method parameter to make it A*.
  * @tparam V the type of the vertices in the graph
  * @param graph the graph to search through, represented as a set of edges between 2 vertices.
  * @param start the starting vertex.
  * @param end the goal vertex.
  * @param heuristic an optional heuristic function `V => Cost`
  */
case class Pathfinder[V](graph: Set[Edge[V]], start: V, end: V, heuristic: V => Cost = (n: V) => 0):
  import scala.collection.mutable as mutable
  private case class CostVertex(vertex: V, cost: Cost)
  private def vertexOrder = new Ordering[CostVertex]:
    def compare(a: CostVertex, b: CostVertex) = b.cost compare a.cost

  /** Finds the shortest from the given start node to the end node in the given graph. */
  def shortestPath =
    val edges = graph.groupBy(_.from)
    val queue = mutable.PriorityQueue(CostVertex(start, 0))(vertexOrder)
    val previous = mutable.Map.empty[V, V]
    val distance = mutable.Map.empty[V, Cost] withDefaultValue Cost.MaxValue    
    distance(start) = 0

    var found = false
    while !found && queue.nonEmpty do
      val min = queue.dequeue.vertex

      if min == end then found = true
      else if edges isDefinedAt min then for edge <- edges(min) do
        val alt = distance(min) + edge.cost
        val destination = edge.to
        if alt < distance(destination) then
          queue.enqueue(CostVertex(destination, alt + heuristic(destination)))
          distance(destination) = alt
          previous(destination) = min

    def backtrack(current: V): Vector[V] =
      if previous(current) == start then Vector(start, current)
      else backtrack(previous(current)) :+ current

    if found then Some(Path(backtrack(end), distance(end))) 
    else None