package com.adventofcode.tasks.t17

import com.adventofcode.Util
import java.lang.Long.max


fun main() {
    part1()
//    part2()
}

private fun part1() {
    val input = Util.readInputForTaskAsLines()
    val moveGenerator = MoveGenerator(input.first())

    val pf = PlayField(moveGenerator)
    for (figure in tetrisFiguresGenerator(2022)) {
        pf.dropFigure(figure)
    }

    println(pf.towerHeight) //3130
}

private fun part2() {
    val input = Util.readInputForTaskAsLines()
//    println(Day17(input).part1())

    val moveGenerator = MoveGenerator(input.first())

    var cnt = 0L

    val pf = PlayField(moveGenerator)
    for (figure in tetrisFiguresGenerator(1_000_000_000_000)) {
        if (cnt++.mod(1000) == 0) {
            pf.clearLowerRows()
        }
        pf.dropFigure(figure)
    }

    println(pf.towerHeight) //3130
}

class PlayField(
    private val moveGenerator: MoveGenerator
) {
    var towerHeight: Long = 0
    private val fieldWidth = 7
    private val droppedFigurePoints = mutableSetOf<Point>()

    private fun figureFallen(f: Figure) {
        towerHeight = max(towerHeight, (f.topBorder + 1))
        droppedFigurePoints.addAll(f.points)
    }

    fun dropFigure(figure: Figure) {
        var f = figure.rebase(getStartPoint())

        while (true) {
            // move left / right
            val direction = moveGenerator.getNextMoveDirection()
            f = makeMove(f, direction)

            // apply gravity
            val downMove = makeMove(f, Move.Down)
            if (downMove != f) {
                f = downMove
                continue
            }

            figureFallen(f)
            break
        }
    }

    private fun makeMove(figure: Figure, nextMoveDirection: Move): Figure {
        val moveCandidate = figure.move(nextMoveDirection)
        return if (isMoveEligible(moveCandidate)) {
            moveCandidate
        } else {
            figure
        }
    }

    // 0123456
    private fun isMoveEligible(moveCandidate: Figure): Boolean {
        // check border collision
        if (moveCandidate.leftBorder < 0 ||
            moveCandidate.rightBorder >= fieldWidth ||
            moveCandidate.bottomBorder < 0
        ) {
            return false
        }

        // check dropped figures collision
        return !moveCandidate.collidesWith(droppedFigurePoints)
    }

    private fun getStartPoint(): Point {
        return Point(2, towerHeight + 3)
    }

    fun clearLowerRows() {
        droppedFigurePoints.removeIf { it.y < towerHeight - 100 }
    }
}

data class Point(
    val x: Long,
    val y: Long
) {
    fun rebase(delta: Point): Point {
        return this.copy(
            x = this.x + delta.x,
            y = this.y + delta.y
        )
    }
}

data class Figure(
    val points: List<Point>
) {
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

private fun tetrisFiguresGenerator(requested: Long): Sequence<Figure> {
    val hLine = Figure(
        listOf(
            Point(0, 0),
            Point(1, 0),
            Point(2, 0),
            Point(3, 0)
        )
    )

    val cross = Figure(
        listOf(
            Point(1, 0),
            Point(0, 1),
            Point(1, 1),
            Point(2, 1),
            Point(1, 2)
        )
    )

    val bottomRightCorner = Figure(
        listOf(
            Point(0, 0),
            Point(1, 0),
            Point(2, 0),
            Point(2, 1),
            Point(2, 2)
        )
    )

    val vLine = Figure(
        listOf(
            Point(0, 0),
            Point(0, 1),
            Point(0, 2),
            Point(0, 3)
        )
    )

    val square = Figure(
        listOf(
            Point(0, 0),
            Point(0, 1),
            Point(1, 0),
            Point(1, 1)
        )
    )

    val figures = listOf(hLine, cross, bottomRightCorner, vLine, square)

    return sequence {
        var returned = 0L
        var completed = false
        while (!completed) {
            for (f in figures) {
                if (++returned > requested) {
                    completed = true
                    break
                }
                yield(f)
            }
        }
    }
}

enum class Move {
    Right, Left, Up, Down
}

class MoveGenerator(private val movesStr: String) {
    private val moves = parseMoves()
    private val movesSeq = generateMoves().iterator()

    fun getNextMoveDirection(): Move {
        return movesSeq.next()
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

    private fun parseMoves(): List<Move> {
        return movesStr.map {
            when (it) {
                '>' -> Move.Right
                '<' -> Move.Left
                else -> throw Exception("Unexpected move $it")
            }
        }
    }
}