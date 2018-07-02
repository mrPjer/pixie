import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Pin(val id: Int)
data class Board(val id: Int)
typealias Counter = Map<Pin, Int>

data class Edge(
        val pin: Pin,
        val board: Board
)

data class NaiveGraph(
        val pins: Set<Pin>,
        val boards: Set<Board>,
        val edges: Set<Edge>
)

data class AdjacencyGraph(
        val pins: List<Pin>,
        val boards: List<Board>,
        val pinsToBoards: Map<Pin, List<Board>>,
        val boardsToPins: Map<Board, List<Pin>>
)

const val NUM_PINS = 2 * 10e4.toInt()
const val NUM_BOARDS = 1 * 10e4.toInt()
const val NUM_EDGES = 17 * 10e4.toInt()
const val NUM_STEPS = 100000

val random = Random(9932L)

fun generateData(): AdjacencyGraph {
    val pins = (1..NUM_PINS).map { Pin(it) }
    val boards = (1..NUM_BOARDS).map { Board(it) }

    val pinsToBoards = HashMap<Pin, MutableList<Board>>(NUM_EDGES)
    val boardsToPins = HashMap<Board, MutableList<Pin>>(NUM_EDGES)

    (1..NUM_EDGES).forEach {
        val pin = pins.sample()
        val board = boards.sample()

        pinsToBoards.computeIfAbsent(pin) { ArrayList() }
        boardsToPins.computeIfAbsent(board) { ArrayList() }

        pinsToBoards[pin]!!.add(board)
        boardsToPins[board]!!.add(pin)
    }

    return AdjacencyGraph(pins, boards, pinsToBoards, boardsToPins)
}

private fun <T> List<T>.sample() = this[random.nextInt(size)]
private fun Counter.top(k: Int) = this
        .toList()
        .sortedByDescending { it.second }
        .take(k)

private fun Counter.printTop(k: Int) = top(k).forEach { System.out.printf(" %s - %d%n", it.first, it.second) }

fun simpleRandomWalk(graph: AdjacencyGraph, startingPin: Pin, numberOfSteps: Int): Counter {
    val counter = HashMap<Pin, Int>()

    var step = 0
    var pin = startingPin

    while (step < numberOfSteps) {
        val board = graph.pinsToBoards[pin].orEmpty().sample()
        val otherPin = graph.boardsToPins[board]!!.sample()

        val oldCount = counter.getOrDefault(otherPin, 0)
        counter[otherPin] = oldCount + 1
        pin = otherPin
        ++step
    }

    return counter
}

fun main(args: Array<String>) {
    val start = System.currentTimeMillis()
    System.out.printf("Generating dataset with %d pins, %d boards and %d edges%n", NUM_PINS, NUM_BOARDS, NUM_EDGES)
    val data = generateData()
    System.out.printf("Done in %d%n", System.currentTimeMillis() - start)

    val startingPin = data.pinsToBoards.keys.toList().sample()

    System.out.printf("Starting pin is %s%n", startingPin)

    val randomWalkTime = System.currentTimeMillis()
    System.out.printf("Starting simple random walk with %d steps%n", NUM_STEPS)
    val randomWalkResult = simpleRandomWalk(data, startingPin, NUM_STEPS)
    System.out.printf("Done in %d%n", System.currentTimeMillis() - randomWalkTime)
    randomWalkResult.printTop(10)
}