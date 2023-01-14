package com.adventofcode.tasks.t14

import com.adventofcode.Util
import java.lang.Integer.max
import java.lang.Integer.min

data class Point(
    val horizontal: Int,
    val depth: Int
) {
    fun getAllPointsInLineTo(other: Point): List<Point> {
        if (this == other) {
            return listOf(this)
        }

        return if (this.depth == other.depth) {
            rangeTo(this.horizontal, other.horizontal).map { h -> Point(h, depth) }
        } else if (this.horizontal == other.horizontal) {
            rangeTo(this.depth, other.depth).map { d -> Point(horizontal, d) }
        } else {
            throw Exception("diagonal line is not supported $this to $other")
        }
    }

    private fun rangeTo(h1: Int, h2: Int): IntRange {
        val start = min(h1, h2)
        val finish = max(h1, h2)

        return start..finish
    }

    fun pointBelow(): Point {
        return copy(depth = this.depth + 1)
    }

    fun pointDiagLeft(): Point {
        return copy(horizontal = this.horizontal - 1, depth = this.depth + 1)
    }

    fun pointDiagRight(): Point {
        return copy(horizontal = this.horizontal + 1, depth = this.depth + 1)
    }
}

abstract class BaseGrid {
    abstract fun execute(): Int
    protected abstract fun isMoveEligible(moveCandidate: Point): Boolean
    protected abstract fun getToClosestPointBelow(sandPoint: Point): Point
    protected fun  applyGravity(sandPoint: Point): Point {
        if (isMoveEligible(sandPoint.pointBelow())) {
            return getToClosestPointBelow(sandPoint)
        }

        if (isMoveEligible(sandPoint.pointDiagLeft())){
            return sandPoint.pointDiagLeft()
        }

        if (isMoveEligible(sandPoint.pointDiagRight())){
            return sandPoint.pointDiagRight()
        }

        return sandPoint
    }
}

class GridNoFloor(
    blockPoints: Set<Point>,
    private val sandSourcePoint: Point
): BaseGrid() {
    private val filledPoints = blockPoints.toMutableSet()
    override fun execute(): Int {
        var sandPoint = sandSourcePoint

        var cnt = 0
        while (pointsBelowExist(sandPoint)) {
            val newPosition = applyGravity(sandPoint)
            if (newPosition != sandPoint) {
                sandPoint = newPosition
                continue
            }

            sandPoint = newPosition

            filledPoints.add(sandPoint)
            cnt++

            sandPoint = sandSourcePoint
        }

        return cnt
    }

    private fun pointsBelowExist(sandPoint: Point): Boolean {
        return filledPoints.any { p -> p.horizontal == sandPoint.horizontal && p.depth > sandPoint.depth }
    }

    override fun isMoveEligible(moveCandidate: Point): Boolean {
        return !filledPoints.contains(moveCandidate)
    }

    override fun getToClosestPointBelow(sandPoint: Point): Point {
        val border = filledPoints
            .filter { it.horizontal == sandPoint.horizontal }
            .filterNot { it.depth < sandPoint.depth }
            .minBy { it.depth }

        return border.copy(depth = border.depth - 1)
    }
}


class GridWithFloor(
    blockPoints: Set<Point>,
    private val sandSourcePoint: Point
): BaseGrid() {
    private val filledPoints = blockPoints.toMutableSet()
    private val floorLevel = blockPoints.maxOf { it.depth } + 2
    override fun execute(): Int {
        var cnt = 0
        var sandPoint = sandSourcePoint

        while (true) {
            val newPosition = applyGravity(sandPoint)
            if (newPosition != sandPoint) {
                sandPoint = newPosition
                continue
            }

            sandPoint = newPosition

            filledPoints.add(sandPoint)
            cnt++

            if (sandPoint == sandSourcePoint) {
                break
            }

            sandPoint = sandSourcePoint
        }

        return cnt
    }

    override fun isMoveEligible(moveCandidate: Point): Boolean {
        if (moveCandidate.depth >= floorLevel) {
            return false
        }

        return !filledPoints.contains(moveCandidate)
    }

    override fun getToClosestPointBelow(sandPoint: Point): Point {
        val border = filledPoints
            .filter { it.horizontal == sandPoint.horizontal }
            .filterNot { it.depth < sandPoint.depth }
            .minByOrNull { it.depth }

        return border?.copy(depth = border.depth - 1)
            ?: Point(sandPoint.horizontal, floorLevel - 1)
    }
}

fun main() {
    part1() //    768
    part2() // 26_686
}

fun part1() {
    val blockPoints = readInput()
    val startPoint = Point(500, 0)

    val grid = GridNoFloor(blockPoints, startPoint)
    val result = grid.execute()

    println(result)
}

fun part2() {
    val blockPoints = readInput()
    val startPoint = Point(500, 0)

    val grid = GridWithFloor(blockPoints, startPoint)
    val result = grid.execute()

    println(result)
}

fun readInput(): Set<Point> {
    val input = Util.readInputForTaskAsLines()
    val wallPoints = mutableSetOf<Point>()

    for (line in input) {
        parseBlock(wallPoints, line)
    }

    return wallPoints
}

fun parseBlock(wallPoints: MutableSet<Point>, line: String) {
    val bindingPoints = line
        .split(" -> ")
        .map { p -> parsePoint(p) }

    for (idx in 0..bindingPoints.size - 2) {
        val start = bindingPoints[idx]
        val end = bindingPoints[idx + 1]

        wallPoints.addAll(start.getAllPointsInLineTo(end))
    }
}

fun parsePoint(p: String): Point {
    val (horizontal, depth) = p
        .split(",")
        .map { it.toInt() }

    return Point(horizontal, depth)
}