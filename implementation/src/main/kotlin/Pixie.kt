import java.util.*
import kotlin.collections.HashSet

typealias Pin = Any
typealias Board = Any

data class Edge(
        val pin: Pin,
        val board: Board
)

data class NaiveGraph(
        val pins: Set<Pin>,
        val boards: Set<Board>,
        val edges: Set<Edge>
)

data class PinToBoard(
        val pin: Pin,
        val board: Board
)

data class BoardToPin(
        val board: Board,
        val pin: Pin
)

data class AdjacencyGraph(
        val pinsToEdges: Set<PinToBoard>,
        val boardsToPins: Set<BoardToPin>
)

const val NUM_PINS = 2 * 10e4.toInt()
const val NUM_BOARDS = 1 * 10e4.toInt()
const val NUM_EDGES = 17 * 10e4.toInt()

val random = Random(9932L)

fun generateData(): AdjacencyGraph {
    val pins = (1..NUM_PINS).map { Pin() }
    val boards = (1..NUM_BOARDS).map { Board() }

    val pinsToBoards = HashSet<PinToBoard>(NUM_EDGES)
    val boardsToPins = HashSet<BoardToPin>(NUM_EDGES)

    (1..NUM_EDGES).forEach {
        val pin = pins[random.nextInt(pins.size)]
        val board = boards[random.nextInt(boards.size)]

        pinsToBoards.add(PinToBoard(pin, board))
        boardsToPins.add(BoardToPin(board, pin))
    }

    return AdjacencyGraph(pinsToBoards, boardsToPins)
}

fun main(args: Array<String>) {
    val start = System.currentTimeMillis()
    System.out.printf("Generating dataset with %d pins, %d boards and %d edges%n", NUM_PINS, NUM_BOARDS, NUM_EDGES)
    val data = generateData()
    System.out.printf("Done in %d%n", System.currentTimeMillis() - start)
}