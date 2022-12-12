package com.adventofcode.tasks.t12

import com.adventofcode.Util
import java.lang.Integer.min


class HeightGrid(private val rawMap: List<List<Int>>) {
    private val gridRightBorder = rawMap.first().size - 1
    private val gridBottomBorder = rawMap.size -1

    fun getPointsOfHeight(height: Int): List<Point> {
        val result = mutableListOf<Point>()

        for (y in rawMap.indices) {
            for (x in rawMap[y].indices) {
                if (rawMap[y][x] == height) {
                    result.add(Point(x, y))
                }
            }
        }

        return result
    }

    fun calcShortestPath(startPoint: Point, endPoint: Point): Int? {
        val visitedPoints = mutableSetOf<Point>()
        visitedPoints.add(startPoint)

        val nextMoves = mutableSetOf<Point>()
        nextMoves.addAll(getNextMovesFromPoint(visitedPoints, startPoint))

        var stepsTaken = 0

        do {
            stepsTaken++

            val currentMoves = nextMoves.toList()
            nextMoves.clear()

            if (currentMoves.contains(endPoint)) {
                return stepsTaken
            }

            currentMoves.forEach { p ->
                visitedPoints.add(p)
                nextMoves.addAll(getNextMovesFromPoint(visitedPoints, p))
            }
        } while (nextMoves.isNotEmpty())

        return null
    }

    private fun getNextMovesFromPoint(visitedPoints: Set<Point>, point: Point): List<Point> {
        return getMovesOnGrid(point)
            .filter { p -> isHeightDifferenceAcceptable(point, p) }
            .filterNot { p -> pointAlreadyVisited(visitedPoints, p) }
    }

    private fun getMovesOnGrid(currentPoint: Point): List<Point> {
        val points = mutableListOf<Point>()

        if (currentPoint.x > 0) {
            points.add(currentPoint.moveLeft())
        }

        if (currentPoint.x < gridRightBorder) {
            points.add(currentPoint.moveRight())
        }

        if (currentPoint.y > 0) {
            points.add(currentPoint.moveUp())
        }

        if (currentPoint.y < gridBottomBorder) {
            points.add(currentPoint.moveDown())
        }

        return points
    }

    private fun isHeightDifferenceAcceptable(from: Point, to: Point): Boolean {
        val p1Height = rawMap[from.y][from.x]
        val p2Height = rawMap[to.y][to.x]

        return p2Height <= p1Height + 1
    }

    private fun pointAlreadyVisited(visitedPoints: Set<Point>, p: Point): Boolean {
        return visitedPoints.contains(p)
    }
}

data class Point(val x: Int, val y: Int) {
    fun moveLeft(): Point {
        return this.copy(x = this.x - 1)
    }
    fun moveRight(): Point {
        return this.copy(x = this.x + 1)
    }
    fun moveUp(): Point {
        return this.copy(y = this.y - 1)
    }
    fun moveDown(): Point {
        return this.copy(y = this.y + 1)
    }
}

fun main() {
    val (grid, p) = parseInput()

    val startPoint = p.first
    val endPoint = p.second

    println(part1(grid, startPoint, endPoint))
    println(part2(grid, endPoint))
}

private fun parseInput(): Pair<HeightGrid, Pair<Point, Point>> {
    val input = Util.readInputForTaskAsLines()

    var startPoint: Point? = null
    var endPoint: Point? = null

    val rawMap = input.mapIndexed { verticalIdx, line ->
        line.mapIndexed { horizontalIdx, point ->
            when (point) {
                'S' -> {
                    startPoint = Point(horizontalIdx, verticalIdx)
                    getHeightForChar('a')
                }
                'E' -> {
                    endPoint = Point(horizontalIdx, verticalIdx)
                    getHeightForChar('z')
                }
                else -> getHeightForChar(point)
            }
        }
    }

    val grid = HeightGrid(rawMap)
    return Pair(grid, Pair(startPoint!!, endPoint!!))
}

private fun part1(grid: HeightGrid, startPoint: Point, endPoint: Point): Int {
    return grid.calcShortestPath(startPoint, endPoint)!!
}

private fun part2(grid: HeightGrid, endPoint: Point): Int {
    val startPoints = grid.getPointsOfHeight(1)

    var minSteps = Int.MAX_VALUE
    for (startPoint in startPoints) {
        val stepsToTarget = grid.calcShortestPath(startPoint, endPoint) ?: Int.MAX_VALUE

        minSteps = min(minSteps, stepsToTarget)
    }

    return minSteps
}

private fun getHeightForChar(c: Char): Int {
    return c.code - 96
}