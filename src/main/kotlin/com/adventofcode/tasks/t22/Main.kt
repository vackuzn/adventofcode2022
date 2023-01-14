package com.adventofcode.tasks.t22

import com.adventofcode.Util
import java.lang.Integer.max

fun main() {
    part1() //  76_332
    part2() // 144_012
}

private fun part1() {
    val input = Util.readInputForTaskAsLines()
    val it = input.iterator()
    val map = readMap(it, FlatWrap())

    val p = Player(map, map.getStartPosition())

    for (move in readMoves(it.next())) {
        p.move(move)
    }

    println(p.getScore())
}

private fun part2() {
    val input = Util.readInputForTaskAsLines()
    val it = input.iterator()
    val map = readMap(it, InputCubeWrapWrap())

    val p = Player(map, map.getStartPosition())

    for (move in readMoves(it.next())) {
        p.move(move)
    }

    println(p.getScore())
}

class PlayField(
    val map: List<List<Int>>,
    private val wrapMode: IWrapMode
) {
    fun getStartPosition(): Position {
        return Position(map[0].indexOfFirst { it >=0 }, 0, Direction.Right)
    }

    private operator fun get(y: Int, x: Int): Int {
        val result = map[y][x]
        assert(result >= 0)

        return result
    }

    private fun movingOverTheEdge(currentPos: Position): Boolean {
        return when (currentPos.direction) {
            Direction.Right -> currentPos.x == getRow(currentPos.y).size - 1 || get(currentPos.y, currentPos.x + 1) == -1
            Direction.Left -> currentPos.x == 0 || get(currentPos.y, currentPos.x - 1) == -1
            Direction.Down -> currentPos.y == map.size - 1 || get(currentPos.y + 1, currentPos.x) == -1
            Direction.Up -> currentPos.y == 0 || get(currentPos.y - 1, currentPos.x) == -1
        }
    }

    fun getRow(y: Int): List<Int> {
        return map[y]
    }

    fun getColumn(x: Int): List<Int> {
        return map.map { row -> row[x] }
    }

    fun move(position: Position, steps: Int): Position {
        var currentPos = position

        for (s in 1..steps) {
            val nextPosition = if (movingOverTheEdge(currentPos)) {
                wrapMode.wrap(this, currentPos)
            } else {
                when (currentPos.direction) {
                    Direction.Right -> moveRight(currentPos)
                    Direction.Down -> moveDown(currentPos)
                    Direction.Left -> moveLeft(currentPos)
                    Direction.Up -> moveUp(currentPos)
                }
            }

            if (runIntoWall(nextPosition)) {
                break
            }

            currentPos = nextPosition
        }

        return currentPos
    }

    private fun runIntoWall(nextPosition: Position): Boolean {
        return get(nextPosition.y, nextPosition.x) == 1
    }

    private fun moveRight(position: Position): Position {
        return position.copy(x = position.x + 1)
    }

    private fun moveLeft(position: Position): Position {
        return position.copy(x = position.x - 1)
    }

    private fun moveUp(position: Position): Position {
        return position.copy(y = position.y - 1)
    }

    private fun moveDown(position: Position): Position {
        return position.copy(y = position.y + 1)
    }
}

enum class Direction(val v: Int) {
    Right(0),
    Down(1),
    Left(2),
    Up(3)
}

data class Position(val x: Int, val y: Int, val direction: Direction)

class Player(private val map: PlayField, startPosition: Position) {
    private var position = startPosition
    fun getScore(): Int {
        return (position.y + 1) * 1000 + (position.x + 1) * 4 + position.direction.v
    }
    fun move(move: IMove) {
        when (move) {
            is RotateMove -> rotate(move.direction)
            is WalkMove -> move(move.steps)
        }
    }

    private fun rotate(direction: RotateDirection) {
        val newDirection = when(position.direction) {
            Direction.Right -> if (direction == RotateDirection.Left) Direction.Up else Direction.Down
            Direction.Down -> if (direction == RotateDirection.Left) Direction.Right else Direction.Left
            Direction.Left -> if (direction == RotateDirection.Left) Direction.Down else Direction.Up
            Direction.Up -> if (direction == RotateDirection.Left) Direction.Left else Direction.Right
        }

        position = position.copy(direction = newDirection)
    }

    private fun move(steps: Int) {
        position = map.move(position, steps)
    }
}

private fun readMap(lines: Iterator<String>, wrap: IWrapMode): PlayField {
    val parsedLines = mutableListOf<List<Int>>()

    var width = 0

    for (line in lines) {
        if (line.isEmpty()) {
            break
        }

        val parsedLine = mutableListOf<Int>()

        for (c in line) {
            when(c) {
                ' ' -> parsedLine.add(-1)
                '.' ->  parsedLine.add(0)
                '#' -> parsedLine.add(1)
            }
        }

        width = max(width, parsedLine.size)
        parsedLines.add(parsedLine)
    }

    val result = parsedLines.map { line ->
        val diff = width - line.size

        if (diff == 0) {
            return@map line
        }

        return@map line + List(diff) { -1 }
    }

    return PlayField(result, wrap)
}

interface IWrapMode {
    fun wrap(playField: PlayField, currentPos: Position): Position
}

class FlatWrap: IWrapMode {
    override fun wrap(playField: PlayField, currentPos: Position): Position {
        return when (currentPos.direction) {
            Direction.Right -> currentPos.copy(x = playField.getRow(currentPos.y).indexOfFirst { it >= 0 })
            Direction.Left -> currentPos.copy(x = playField.getRow(currentPos.y).indexOfLast { it >= 0 })
            Direction.Down -> currentPos.copy(y = playField.getColumn(currentPos.x).indexOfFirst { it >= 0 })
            Direction.Up -> currentPos.copy(y = playField.getColumn(currentPos.x).indexOfLast { it >= 0 })
        }
    }
}
// 162186 =
class ExampleCubeWrap: IWrapMode {
    override fun wrap(playField: PlayField, currentPos: Position): Position {
        val borderLength = playField.getRow(0).count { it >= 0 }

        fun normalize(crd: Int): Int {
            return crd.mod(borderLength)
        }

        fun reverse(crd: Int): Int {
            return borderLength - 1 - crd.mod(borderLength)
        }

        fun offset(n: Int): Int {
            return borderLength * n
        }

        // up
        if (currentPos.direction == Direction.Up) {
            if (currentPos.x < offset(1)) {
                println("u1 $currentPos")
                return currentPos.copy(
                    x = reverse(currentPos.x) + offset(2),
                    y = 0,
                    direction = Direction.Down
                )
            }

            if (currentPos.x < offset(2)) {
                println("u2 $currentPos")
                return currentPos.copy(
                    x = offset(2),
                    y = normalize(currentPos.x),
                    direction = Direction.Right
                )
            }

            if (currentPos.x < offset(3)) {
                println("u3 $currentPos")
                return currentPos.copy(
                    x = reverse(currentPos.x),
                    y = offset(1),
                    direction = Direction.Down
                )
            }

            println("u4 $currentPos")
            return currentPos.copy(
                x = offset(3) - 1,
                y = reverse(currentPos.x) + offset(1),
                direction = Direction.Left
            )
        }

        // down
        if (currentPos.direction == Direction.Down) {
            if (currentPos.x < offset(1)) {
                println("d1 $currentPos")
                return currentPos.copy(
                    x = reverse(currentPos.x) + offset(2),
                    y = playField.map.size - 1,
                    direction = Direction.Up
                )
            }

            if (currentPos.x < offset(2)) {
                println("d2 $currentPos")
                return currentPos.copy(
                    x = offset(2),
                    y = normalize(currentPos.x) + offset(2),
                    direction = Direction.Right
                )
            }

            if (currentPos.x < offset(3)) {
                println("d3 $currentPos")
                return currentPos.copy(
                    x = reverse(currentPos.x),
                    y = offset(2) - 1,
                    direction = Direction.Up
                )
            }

            println("d4 $currentPos")
            return currentPos.copy(
                x = 0,
                y = reverse(currentPos.x) + offset(1),
                direction = Direction.Right
            )
        }

        // Left
        if (currentPos.direction == Direction.Left) {
            if (currentPos.y < offset(1)) {
                println("l1 $currentPos")
                return currentPos.copy(
                    x = normalize(currentPos.y) + offset(1),
                    y = offset(1),
                    direction = Direction.Down
                )
            }

            if (currentPos.y < offset(2)) {
                println("l2 $currentPos")
                return currentPos.copy(
                    x = reverse(currentPos.y) + offset(3),
                    y = offset(3) - 1,
                    direction = Direction.Up
                )
            }

            println("l3 $currentPos")
            return currentPos.copy(
                x = reverse(currentPos.y) + offset(1),
                y = offset(2) - 1,
                direction = Direction.Up
            )
        }

        // Right
        if (currentPos.direction == Direction.Right) {
            if (currentPos.y < offset(1)) {
                println("r1 $currentPos")
                return currentPos.copy(
                    x = offset(4) - 1,
                    y = reverse(currentPos.y) + offset(2),
                    direction = Direction.Left
                )
            }

            if (currentPos.y < offset(2)) {
                println("r2 $currentPos")
                return currentPos.copy(
                    x = reverse(currentPos.y) + offset(3),
                    y = offset(2) - 1,
                    direction = Direction.Down
                )
            }

            println("r3 $currentPos")
            return currentPos.copy(
                x = offset(2),
                y = reverse(currentPos.y),
                direction = Direction.Left
            )
        }

        TODO()
    }
}

class InputCubeWrapWrap: IWrapMode {
    override fun wrap(playField: PlayField, currentPos: Position): Position {
        val borderLength = playField.getRow(0).count { it >= 0 } / 2

        fun normalize(crd: Int): Int {
            return crd.mod(borderLength)
        }

        fun reverse(crd: Int): Int {
            return borderLength - 1 - crd.mod(borderLength)
        }

        fun offset(n: Int): Int {
            return borderLength * n
        }

        // up
        if (currentPos.direction == Direction.Up) {
            if (currentPos.x < offset(1)) {
                println("u1 $currentPos")
                return currentPos.copy(
                    x = offset(1),
                    y = normalize(currentPos.x) + offset(1),
                    direction = Direction.Right
                )
            }

            if (currentPos.x < offset(2)) {
                println("u2 $currentPos")
                return currentPos.copy(
                    x = 0,
                    y = normalize(currentPos.x) + offset(3),
                    direction = Direction.Right
                )
            }

            println("u3 $currentPos")
            return currentPos.copy(
                x = normalize(currentPos.x),
                y = offset(4) - 1,
                direction = Direction.Up
            )
        }

        // down
        if (currentPos.direction == Direction.Down) {
            if (currentPos.x < offset(1)) {
                println("d1 $currentPos")
                return currentPos.copy(
                    x = normalize(currentPos.x) + offset(2),
                    y = 0,
                    direction = Direction.Down
                )
            }

            if (currentPos.x < offset(2)) {
                println("d2 $currentPos")
                return currentPos.copy(
                    x = offset(1) - 1,
                    y = normalize(currentPos.x) + offset(3),
                    direction = Direction.Left
                )
            }

            println("d3 $currentPos")
            return currentPos.copy(
                x = offset(2) - 1,
                y = normalize(currentPos.x) + offset(1),
                direction = Direction.Left
            )
        }

        // Left
        if (currentPos.direction == Direction.Left) {
            if (currentPos.y < offset(1)) {
                println("l1 $currentPos")
                return currentPos.copy(
                    x = 0,
                    y = reverse(currentPos.y) + offset(2),
                    direction = Direction.Right
                )
            }

            if (currentPos.y < offset(2)) {
                println("l2 $currentPos")
                return currentPos.copy(
                    x = normalize(currentPos.y),
                    y = offset(2),
                    direction = Direction.Down
                )
            }

            if (currentPos.y < offset(3)) {
                println("l3 $currentPos")
                return currentPos.copy(
                    x = offset(1),
                    y = reverse(currentPos.y),
                    direction = Direction.Right
                )
            }

            println("l4 $currentPos")
            return currentPos.copy(
                x = normalize(currentPos.y) + offset(1),
                y = 0,
                direction = Direction.Down
            )
        }

        // Right
        if (currentPos.direction == Direction.Right) {
            if (currentPos.y < offset(1)) {
                println("l1 $currentPos")
                return currentPos.copy(
                    x = offset(2) - 1,
                    y = reverse(currentPos.y) + offset(2),
                    direction = Direction.Left
                )
            }

            if (currentPos.y < offset(2)) {
                println("l2 $currentPos")
                return currentPos.copy(
                    x = normalize(currentPos.y) + offset(2),
                    y = offset(1) - 1,
                    direction = Direction.Up
                )
            }

            if (currentPos.y < offset(3)) {
                println("l3 $currentPos")
                return currentPos.copy(
                    x = offset(3) - 1,
                    y = reverse(currentPos.y),
                    direction = Direction.Left
                )
            }

            println("l4 $currentPos")
            return currentPos.copy(
                x = normalize(currentPos.y) + offset(1),
                y = offset(3) - 1,
                direction = Direction.Up
            )
        }

        TODO()
    }
}

enum class RotateDirection { Left, Right }

interface IMove
data class RotateMove(val direction: RotateDirection): IMove
data class WalkMove(val steps: Int): IMove

private fun readMoves(moves: String): Sequence<IMove> {
    return sequence {
        var pos = 0

        while (pos in moves.indices) {
            if (moves[pos].isDigit()) {
                val stepsStr = "\\d+"
                    .toRegex()
                    .find(moves.substring(pos))!!
                    .value

                yield(WalkMove(stepsStr.toInt()))

                pos += stepsStr.length
            }

            if (pos in moves.indices) {
                val rotateCode = moves[pos++]
                val direction = when (rotateCode) {
                    'R' -> RotateDirection.Right
                    'L' -> RotateDirection.Left
                    else -> throw Exception("Unexpected move code $rotateCode")
                }

                yield(RotateMove(direction))
            }
        }
    }
}