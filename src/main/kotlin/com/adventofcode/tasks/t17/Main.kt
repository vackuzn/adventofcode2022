package com.adventofcode.tasks.t17

import com.adventofcode.Util
import java.lang.Long.max


fun main() {
    part1() //             3_130
    part2() // 1_556_521_739_139
}

private fun part1() {
    val game = Game()
    val result = game.play(2022)

    println(result)
}

private fun part2() {
    val game = Game()
    val result = game.play(1_000_000_000_000)

    println(result)
}

class Game {
    private val moveGenerator = MoveGenerator.load()                    // generates moves from the input (left, right)
    private val playField = PlayField(moveGenerator)                    // game logic simulation: figures behavior, returns stats
    private val figureGenerator = TetrisFiguresGenerator()              // generates figures
    private val seenStates = mutableMapOf<State, Pair<Long, Long>>()    // remember states for fast-forward
    private var figuresLeft = 0L
    private var heightOffset = 0L

    fun play(figureCount: Long): Long {
        figuresLeft = figureCount
        var fastForwardPerformed = false

        while (figuresLeft-- > 0) {
            val figure = figureGenerator.generateFigure()
            playField.dropFigure(figure)

            if (!fastForwardPerformed) {
                fastForwardPerformed = tryPerformFastForward()
            }
        }

        return playField.towerHeight + heightOffset
    }

    // Remember state: cap of the tower, move offset, figure offset
    // If state repeats once, it will always repeat
    // Once we find repetition, we skip though all repetitions
    // then calculate only the rest of the figures
    private fun tryPerformFastForward(): Boolean {
        val state = getCurrentGameState()

        if (stateSeenBefore(state)) {
            fastForward(state)
            return true
        }

        rememberState(state)
        return false
    }

    private fun getCurrentGameState(): State {
        return State(
            playField.getTowerCapHash(),
            figureGenerator.getLastFigureOffset(),
            moveGenerator.getLastMoveOffset()
        )
    }

    private fun stateSeenBefore(state: State): Boolean {
        return seenStates.contains(state)
    }

    private fun fastForward(state: State) {
        val (prevFiguresCount, prevTowerHeight) = seenStates[state]!!

        // delta height and figure count for the full period between same states
        val deltaCount = playField.figuresCount - prevFiguresCount
        val deltaHeight = playField.towerHeight - prevTowerHeight

        val fullRepetitionsCount = figuresLeft / deltaCount

        // fast-forward all full periods that fit
        heightOffset = fullRepetitionsCount * deltaHeight
        figuresLeft -= fullRepetitionsCount * deltaCount
    }

    private fun rememberState(state: State) {
        seenStates[state] = Pair(playField.figuresCount, playField.towerHeight)
    }
}

data class State(
    val towerCapHash: Int,
    val lastFigureOffset: Int,
    val lastMoveOffset: Int
)

class PlayField(private val moveGenerator: MoveGenerator) {
    var towerHeight: Long = 0
    var figuresCount = 0L
    private val fieldWidth = 7
    private val droppedFigurePoints = mutableSetOf<Point>()
    private val clearBlocksDeeperThan = 100
    private val clearEvery = 100

    fun getTowerCapHash(): Int {
        val topPoints = droppedFigurePoints
            .groupBy { p -> p.x }
            .map { xPoints -> Point(x = xPoints.key, y = xPoints.value.maxOf { it.y }) }
            .toSet()

        val cutBelow = topPoints.minOf { it.y }
        val capPoints = droppedFigurePoints
            .filterNot { it.y < cutBelow }
            .map { Point(it.x, it.y - cutBelow) }

        return capPoints.hashCode()
    }

    fun dropFigure(figure: Figure) {
        var figureLocation = figure.rebase(getStartPoint())

        while (true) {
            // move left / right
            val direction = moveGenerator.getNextMoveDirection()
            figureLocation = makeMove(figureLocation, direction)

            // apply gravity
            val downMove = makeMove(figureLocation, Move.Down)
            if (downMove != figureLocation) {
                figureLocation = downMove
                continue
            }

            figureFallen(figureLocation)
            break
        }

        figuresCount++
        clearBottom()
    }

    private fun getStartPoint(): Point {
        return Point(2, towerHeight + 3)
    }

    private fun makeMove(figure: Figure, nextMoveDirection: Move): Figure {
        val moveCandidate = figure.move(nextMoveDirection)
        return if (isMoveEligible(moveCandidate)) {
            moveCandidate
        } else {
            figure
        }
    }

    private fun isMoveEligible(moveCandidate: Figure): Boolean {
        val collidesWithWalls = moveCandidate.leftBorder < 0 ||
                moveCandidate.rightBorder >= fieldWidth ||
                moveCandidate.bottomBorder < 0

        if (collidesWithWalls) {
            return false
        }

        // check dropped figures collision
        val collidesWithFloorOrOtherFigures = moveCandidate.collidesWith(droppedFigurePoints)

        return !collidesWithFloorOrOtherFigures
    }

    private fun figureFallen(f: Figure) {
        towerHeight = max(towerHeight, (f.topBorder + 1))
        droppedFigurePoints.addAll(f.points)
    }

    private fun clearBottom() {
        if (figuresCount % clearEvery != 0L) {
            return
        }

        droppedFigurePoints.removeIf { it.y < towerHeight - clearBlocksDeeperThan }
    }
}

data class Point(val x: Long, val y: Long) {
    fun rebase(delta: Point): Point {
        return this.copy(
            x = this.x + delta.x,
            y = this.y + delta.y
        )
    }
}

data class Figure(val points: List<Point>) {
    val rightBorder: Long = points.maxOf { it.x }
    val leftBorder: Long = points.minOf { it.x }
    val topBorder: Long = points.maxOf { it.y }
    val bottomBorder: Long = points.minOf { it.y }

    fun rebase(delta: Point): Figure {
        val rebasedPts = points.map { it.rebase(delta) }
        return this.copy(points = rebasedPts)
    }

    fun move(direction: Move): Figure {
        val rebaseDelta = when(direction) {
            Move.Right -> Point(1, 0)
            Move.Left -> Point(-1, 0)
            Move.Up -> Point(0, 1)
            Move.Down -> Point(0, -1)
        }

        return rebase(rebaseDelta)
    }

    fun collidesWith(droppedFigurePoints: Set<Point>): Boolean {
        return droppedFigurePoints.any { points.contains(it) }
    }
}

class TetrisFiguresGenerator {
    companion object {
        private val hLine = Figure(
            listOf(
                Point(0, 0),
                Point(1, 0),
                Point(2, 0),
                Point(3, 0)
            )
        )

        private val cross = Figure(
            listOf(
                Point(1, 0),
                Point(0, 1),
                Point(1, 1),
                Point(2, 1),
                Point(1, 2)
            )
        )

        private val bottomRightCorner = Figure(
            listOf(
                Point(0, 0),
                Point(1, 0),
                Point(2, 0),
                Point(2, 1),
                Point(2, 2)
            )
        )

        private val vLine = Figure(
            listOf(
                Point(0, 0),
                Point(0, 1),
                Point(0, 2),
                Point(0, 3)
            )
        )

        private val square = Figure(
            listOf(
                Point(0, 0),
                Point(0, 1),
                Point(1, 0),
                Point(1, 1)
            )
        )

        val figures = listOf(hLine, cross, bottomRightCorner, vLine, square)
    }

    private val figuresIterator = createInfiniteFigureSequence().iterator()
    private var lastFigureOffset = 0

    fun getLastFigureOffset(): Int {
        return lastFigureOffset
    }

    fun generateFigure(): Figure {
        return figuresIterator.next()
    }

    private fun createInfiniteFigureSequence(): Sequence<Figure> {
        return sequence {
            while (true) {
                for (f in figures) {
                    lastFigureOffset = (lastFigureOffset + 1) % figures.count()
                    yield(f)
                }
            }
        }
    }
}

enum class Move {
    Right, Left, Up, Down
}

class MoveGenerator(private val movesStr: String) {
    companion object {
        fun load(): MoveGenerator {
            val moves = Util.readInputForTaskAsLines().first()
            return MoveGenerator(moves)
        }
    }

    private var moveOffset = 0
    private val moves = parseMoves()
    private val movesSeq = generateMoves().iterator()

    fun getNextMoveDirection(): Move {
        moveOffset++
        moveOffset = (moveOffset + 1) % movesStr.length

        return movesSeq.next()
    }

    fun getLastMoveOffset(): Int {
        return moveOffset
    }

    private fun parseMoves(): List<Move> {
        return movesStr.map {
            when (it) {
                '>' -> Move.Right
                '<' -> Move.Left
                else -> throw Exception("Unexpected move $it")
            }
        }
    }

    private fun generateMoves(): Sequence<Move> {
        return sequence {
            while (true) {
                for (m in moves) {
                    yield(m)
                }
            }
        }
    }
}