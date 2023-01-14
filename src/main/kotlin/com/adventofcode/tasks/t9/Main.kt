package com.adventofcode.tasks.t9

import com.adventofcode.Util
import kotlin.math.sign

data class Point(val x: Int, val y: Int)

enum class MoveDirection {
    Left,
    Right,
    Up,
    Down;

    fun isHorizontal(): Boolean {
        return this in listOf(Left, Right)
    }
}

class Grid {
    fun getPositionAfterMove(point: Point, direction: MoveDirection): Point {
        val coefficient = when (direction) {
            MoveDirection.Right -> 1
            MoveDirection.Up -> 1
            MoveDirection.Left -> -1
            MoveDirection.Down -> -1
        }

        return if (direction.isHorizontal()) {
            point.copy(x = point.x + coefficient)
        } else {
            point.copy(y = point.y + coefficient)
        }
    }

    fun isAdjacent(p1: Point, p2: Point): Boolean {
        val adjacentPointsOfP1 = listOf(
            p1.copy(y = p1.y + 1, x = p1.x - 1),
            p1.copy(y = p1.y + 1),
            p1.copy(y = p1.y + 1, x = p1.x + 1),

            p1.copy(x = p1.x - 1),
            p1,
            p1.copy(x = p1.x + 1),

            p1.copy(y = p1.y - 1, x = p1.x - 1),
            p1.copy(y = p1.y - 1),
            p1.copy(y = p1.y - 1, x = p1.x + 1)
        )

        return p2 in adjacentPointsOfP1
    }

    fun moveTowards(toMove: Point, target: Point): Point {
        val xDif = target.x - toMove.x
        val yDif = target.y - toMove.y

        val xPortion = xDif.sign
        val yPortion = yDif.sign

        return toMove.copy(x = toMove.x + xPortion, y = toMove.y + yPortion)
    }
}

fun main() {
    val input = Util.readInputForTaskAsLines()
    val grid = Grid()

    part1(input, grid)
    part2(input, grid)
}

private fun part1(input: List<String>, grid: Grid) {
    val snake = MutableList(2) { Point(0,0) }
    val uniqueTailLocationCount = moveSnake(input, grid, snake)

    println(uniqueTailLocationCount) // 6018
}

private fun part2(input: List<String>, grid: Grid) {
    val snake = MutableList(10) { Point(0,0) }
    val uniqueTailLocationCount = moveSnake(input, grid, snake)

    println(uniqueTailLocationCount) // 2619
}

private fun moveSnake(input: List<String>, grid: Grid, snake: MutableList<Point>): Int {
    val uniqueTailLocations = mutableSetOf(snake.last())

    for (line in input) {
        val (direction, moveCount) = parseLine(line)

        for (move in moveCount downTo 1) {
            snake[0] = grid.getPositionAfterMove(snake.first(), direction)

            for (i in 1 until snake.size) {
                if (grid.isAdjacent(snake[i - 1], snake[i])) {
                    break
                }

                snake[i] = grid.moveTowards(snake[i], snake[i - 1])
            }

            uniqueTailLocations.add(snake.last())

        }
    }

    return uniqueTailLocations.count()
}

fun parseLine(line: String): Pair<MoveDirection, Int> {
    val s = line.split(" ")
    val direction = when (s.first()) {
        "L" -> MoveDirection.Left
        "R" -> MoveDirection.Right
        "U" -> MoveDirection.Up
        "D" -> MoveDirection.Down
        else -> throw Exception("Unexpected direction ${s.first()}")
    }
    val moveCount = s.last().toInt()

    return Pair(direction, moveCount)
}
