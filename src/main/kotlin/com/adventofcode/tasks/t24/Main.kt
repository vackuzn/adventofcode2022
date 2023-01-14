package com.adventofcode.tasks.t24

import com.adventofcode.Util

class Field(
    private val rightBorder: Int,
    private val bottomBorder: Int,
    private val start: Point,
    private val finish: Point,
    private val blizzards: MutableList<Blizzard>
) {
    fun moveForward(): Int {
        return move(start, finish)
    }

    fun moveBack(): Int {
        return move(finish, start)
    }

    private fun move(start: Point, finish: Point): Int {
        var minutesElapsed = 0
        val currentMoves = mutableSetOf<Point>()

        do {
            moveBlizzards()
            val finishReached = makeMoves(currentMoves, start, finish)
            minutesElapsed++
        } while (!finishReached)

        return minutesElapsed
    }

    private fun moveBlizzards() {
        blizzards.forEach { it.moveBlizzard() }
    }

    private fun makeMoves(currentMoves: MutableSet<Point>, start: Point, finish: Point): Boolean {
        val blizzardPositions = blizzards.map { it.position }.toSet()

        val nextMoveCandidates = (currentMoves + start).flatMap { it.getPossibleMoves() }

        if (nextMoveCandidates.contains(finish)) {
            return true
        }

        val nextMoves = nextMoveCandidates
            .filterNot { it.x == 0 || it.y == 0 || it.x == rightBorder || it.y == bottomBorder }
            .filterNot { blizzardPositions.contains(it) }

        currentMoves.clear()
        currentMoves.addAll(nextMoves)

        return false
    }
}

enum class Direction { Up, Down, Left, Right }

data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction): Point {
        return when (direction) {
            Direction.Up    -> copy(y = y - 1)
            Direction.Down  -> copy(y = y + 1)
            Direction.Left  -> copy(x = x - 1)
            Direction.Right -> copy(x = x + 1)
        }
    }

    fun getPossibleMoves(): List<Point> {
        return Direction
            .values()
            .map { move(it) } + this
    }
}

class Blizzard(
    var position: Point,
    private val direction: Direction,
    private val border: Int
) {
    fun moveBlizzard() {
        val nextPositionCandidate = position.move(direction)

        val nextPosition = when (direction) {
            Direction.Up    -> if (nextPositionCandidate.y == 0) position.copy(y = border - 1) else nextPositionCandidate
            Direction.Down  -> if (nextPositionCandidate.y == border) position.copy(y = 1) else nextPositionCandidate
            Direction.Left  -> if (nextPositionCandidate.x == 0) position.copy(x = border - 1) else nextPositionCandidate
            Direction.Right -> if (nextPositionCandidate.x == border) position.copy(x = 1) else nextPositionCandidate
        }

        position = nextPosition
    }
}

fun main() {
    part1() // 301
    part2() // 859
}

fun part1() {
    val field = readField()

    val result = field.moveForward()

    println(result)
}
fun part2() {
    val field = readField()

    var result = field.moveForward()
    result += field.moveBack()
    result += field.moveForward()

    println(result)
}

private fun readField(): Field {
    val input = Util.readInputForTaskAsLines()

    val rightBorder = input.first().length - 1
    val bottomBorder = input.size - 1

    val xStart = input.first().indexOf(".")
    val xFinish = input.last().indexOf(".")

    val blizzards = getBlizzards(input, rightBorder, bottomBorder)

    return Field(
        rightBorder,
        bottomBorder,
        Point(xStart, 0),
        Point(xFinish, bottomBorder),
        blizzards
    )
}

private fun getBlizzards(input: List<String>, rightBorder: Int, bottomBorder: Int): MutableList<Blizzard> {
    val blizzards = mutableListOf<Blizzard>()

    input.forEachIndexed { y, line ->
        line.forEachIndexed { x, c ->
            val direction = when(c) {
                '<' -> Direction.Left
                '>' -> Direction.Right
                '^' -> Direction.Up
                'v' -> Direction.Down
                else -> null
            }

            if (direction != null) {
                val limit = when (direction) {
                    Direction.Up -> bottomBorder
                    Direction.Down -> bottomBorder
                    Direction.Left -> rightBorder
                    Direction.Right -> rightBorder
                }

                val blizzard = Blizzard(
                    Point(x, y),
                    direction,
                    limit)

                blizzards.add(blizzard)
            }
        }
    }

    return blizzards
}