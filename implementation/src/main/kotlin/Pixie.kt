import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Pin(val id: Int)
data class Board(val id: Int)
typealias Counter = Map<Pin, Int>

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

fun generateWeights(pinToBoards: Map<Pin, List<Board>>) = pinToBoards.flatMap { (pin, boards) ->
    boards.map { board ->
        (pin to board) to random.nextDouble()
    }
}.toMap()

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

private fun <T, R, V, U> List<T>.sample(
        startingElement: R,
        weights: Map<Pair<V, U>, Double>,
        combiner: (R, T) -> Pair<V, U>
): T {
    val totalWeight = map { weights[combiner(startingElement, it)]!! }.sum()
    val selectedWeight = random.nextDouble() * totalWeight

    var traversedWeight = 0.0
    forEach {
        traversedWeight += weights[combiner(startingElement, it)]!!

        if (traversedWeight >= selectedWeight) {
            return it
        }
    }
    throw RuntimeException("Roulette wheel selection failed to select an element")
}

private fun List<Board>.sample(startingPin: Pin, weights: Map<Pair<Pin, Board>, Double>): Board =
        sample(startingPin, weights) { i, j -> Pair(i, j) }

private fun List<Pin>.sample(startingBoard: Board, weights: Map<Pair<Pin, Board>, Double>): Pin =
        sample(startingBoard, weights) { i, j -> Pair(j, i) }

fun weightedRandomWalk(graph: AdjacencyGraph, startingPin: Pin, numberOfSteps: Int, weights: Map<Pair<Pin, Board>, Double>): Counter {
    val counter = HashMap<Pin, Int>()

    var step = 0
    var pin = startingPin

    while (step < numberOfSteps) {
        val board = graph.pinsToBoards[pin].orEmpty().sample(pin, weights)
        val otherPin = graph.boardsToPins[board]!!.sample(board, weights)

        val oldCount = counter.getOrDefault(otherPin, 0)
        counter[otherPin] = oldCount + 1
        pin = otherPin
        ++step
    }

    return counter
}

fun weightedRandomWalk(graph: AdjacencyGraph, startingPins: List<Pair<Pin, Double>>, numberOfSteps: Int, weights: Map<Pair<Pin, Board>, Double>): List<Counter> {
    val maxDegree = graph.pinsToBoards.map { it.value.size }.max()!!

    val scalingFactorSum = startingPins.map { (pin, _) ->
        val degree = graph.pinsToBoards[pin]!!.size
        degree * (maxDegree - Math.log(degree.toDouble()))
    }.sum()

    return startingPins.map { (pin, weight) ->
        val degree = graph.pinsToBoards[pin]!!.size
        val scalingFactor = degree * (maxDegree - Math.log(degree.toDouble()))
        val walkLength = Math.round(weight * numberOfSteps * scalingFactor / scalingFactorSum).toInt()
        weightedRandomWalk(graph, pin, walkLength, weights)
    }
}

private fun List<Counter>.combine(): Counter = flatMap { it.keys }
        .map { pin -> pin to Math.pow(this.map { Math.sqrt(it.getOrDefault(pin, 0).toDouble()) }.sum(), 2.0).toInt() }
        .toMap()

fun main(args: Array<String>) {
    val start = System.currentTimeMillis()
    System.out.printf("Generating dataset with %d pins, %d boards and %d edges%n", NUM_PINS, NUM_BOARDS, NUM_EDGES)
    val data = generateData()
    val weights = generateWeights(data.pinsToBoards)
    System.out.printf("Done in %d%n", System.currentTimeMillis() - start)

    val startingPin = data.pinsToBoards.keys.toList().sample()

    System.out.printf("Starting pin is %s%n", startingPin)

    val randomWalkTime = System.currentTimeMillis()
    System.out.printf("Starting simple random walk with %d steps%n", NUM_STEPS)
    val randomWalkResult = simpleRandomWalk(data, startingPin, NUM_STEPS)
    System.out.printf("Done in %d%n", System.currentTimeMillis() - randomWalkTime)
    randomWalkResult.printTop(10)

    val weightedRandomWalkTime = System.currentTimeMillis()
    System.out.printf("Starting weighted random walk with %d steps%n", NUM_STEPS)
    val weightedRandomWalkResult = weightedRandomWalk(data, startingPin, NUM_STEPS, weights)
    System.out.printf("Done in %d%n", System.currentTimeMillis() - weightedRandomWalkTime)
    weightedRandomWalkResult.printTop(10)

    val multiplePinQuery = (1..5)
            .map { data.pinsToBoards.keys.toList().sample() }
            .zip((1..5).map { 0.2 * it })
    val multiplePinWalkTime = System.currentTimeMillis()
    System.out.printf("Starting multiple pin walk with pins %s and %d steps%n", multiplePinQuery, NUM_STEPS)
    val multiplePinWalkResult = weightedRandomWalk(data, multiplePinQuery, NUM_STEPS, weights)
    System.out.printf("Done in %d%n", System.currentTimeMillis() - multiplePinWalkTime)
    multiplePinWalkResult.forEach { it.printTop(3); System.out.println() }

    System.out.printf("Combined result%n")
    multiplePinWalkResult.combine().printTop(5)
}