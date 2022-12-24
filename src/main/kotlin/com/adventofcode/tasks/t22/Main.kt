package com.adventofcode.tasks.t22

import com.adventofcode.Util

fun main() {
    val input = Util.readInputForTaskAsLines()
    val it = input.iterator()
    val map = readMap(it)

    val p = Player(map, map.getStartPosition())

    for (move in readMoves(it.next())) {
        p.move(move)
    }

    println(p.getScore())

    val aaa = 5
}

class PlayField(
    private val map: List<List<Int>>,
    private val lineOffsets: List<Int>
) {
    fun getStartPosition(): Position {
        return Position(lineOffsets[0], 0, Direction.Right)
    }

    private operator fun get(y: Int, x: Int): Int {
        return map[y][x - lineOffsets[y]]
    }

    fun move(position: Position, steps: Int): Position {
        return when (position.direction) {
            Direction.Right -> moveRight(position, steps)
            Direction.Down -> moveDown(position, steps)
            Direction.Left -> moveLeft(position, steps)
            Direction.Up -> moveUp(position, steps)
        }
    }

    private fun moveRight(position: Position, steps: Int): Position {
        val xIdx = position.x - lineOffsets[position.y]

        val line = map[position.y]
        val newIdx = moveOnLine(line, xIdx, 1, steps)

        return position.copy(x = newIdx + lineOffsets[position.y])
    }

    private fun moveLeft(position: Position, steps: Int): Position {
        val xIdx = position.x - lineOffsets[position.y]

        val line = map[position.y]
        val newIdx = moveOnLine(line, xIdx, -1, steps)

        return position.copy(x = newIdx + lineOffsets[position.y])
    }

    private fun moveUp(position: Position, steps: Int): Position {
        val (offset, line) = getVertical(position.x)
        val yIdx = position.y - offset

        val newIdx = moveOnLine(line, yIdx, -1, steps)

        return position.copy(y = newIdx + offset)
    }

    private fun moveDown(position: Position, steps: Int): Position {
        val (offset, line) = getVertical(position.x)
        val yIdx = position.y - offset

        val newIdx = moveOnLine(line, yIdx, 1, steps)

        return position.copy(y = newIdx + offset)
    }

    private fun getVertical(x: Int): Pair<Int, List<Int>> {
        val existingCells = List(map.size) { idx ->
            if (x < lineOffsets[idx]) {
                return@List Pair(idx, false)
            }

            if (x >= lineOffsets[idx] + map[idx].size) {
                return@List Pair(idx, false)
            }

            return@List Pair(idx, true)
        }

        val start = existingCells.first { (_, existing) -> existing }.first
        val end = existingCells.last { (_, existing) -> existing }.first

        val line = (start..end).map { idx -> get(idx, x) }

        return Pair(start, line)
    }

    private fun moveOnLine(line: List<Int>, startIdx: Int, d: Int, steps: Int): Int {
        var idx = startIdx

        for (stepNum in 1..steps) {
            val nextIdx = (idx + d).mod(line.size)

            if (line[nextIdx] == 0) {
                idx = nextIdx
            } else {
                break
            }
        }

        return idx
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

private fun readMap(lines: Iterator<String>): PlayField {
    val lineOffsets = mutableListOf<Int>()
    val parsedLines = mutableListOf<List<Int>>()

    for (line in lines) {
        if (line.isEmpty()) {
            break
        }

        var offset = 0
        val parsedLine = mutableListOf<Int>()

        for (c in line) {
            when(c) {
                ' ' -> offset++
                '.' ->  parsedLine.add(0)
                '#' -> parsedLine.add(1)
            }
        }

        lineOffsets.add(offset)
        parsedLines.add(parsedLine)
    }

    return PlayField(parsedLines, lineOffsets)




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