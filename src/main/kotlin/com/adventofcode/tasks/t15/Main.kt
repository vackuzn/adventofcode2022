package com.adventofcode.tasks.t15

import com.adventofcode.Util
import kotlin.math.absoluteValue
import kotlin.time.ExperimentalTime


data class Point(val x: Int, val y: Int) {
    fun manhattanDistanceTo(other: Point): Int {
        return (this.x - other.x).absoluteValue + (this.y - other.y).absoluteValue
    }

    fun lineTo(other: Point): Set<Point> {
        if (this == other) {
            return setOf(this)
        }

        return if (this.x == other.x) {
            rangeTo(this.y, other.y).map { Point(this.x, it) }.toSet()
        } else if (this.y == other.y) {
            rangeTo(this.x, other.x).map { Point(it, this.y) }.toSet()
        } else {
            throw Exception("diagonal line is not supported $this to $other")
        }
    }

    private fun rangeTo(h1: Int, h2: Int): IntRange {
        val start = Integer.min(h1, h2)
        val finish = Integer.max(h1, h2)

        return start..finish
    }

    fun getPointsOnCircleWithRadius(radius: Int, filter: (x: Int, y: Int) -> Boolean): MutableSet<Point> {
        if (radius == 0) {
            return mutableSetOf(this)
        }

        val result = mutableSetOf<Point>()

        // border cases
        addPointIfSatisfiesFilter(x, y + radius, result, filter)
        addPointIfSatisfiesFilter(x, y - radius, result, filter)
        addPointIfSatisfiesFilter(x + radius, y, result, filter)
        addPointIfSatisfiesFilter(x - radius, y, result, filter)

        for (i in 1 until radius) {
            addPointIfSatisfiesFilter(x + i, y + (radius - i), result, filter)
            addPointIfSatisfiesFilter(x + i, y - (radius - i), result, filter)
            addPointIfSatisfiesFilter(x - i, y + (radius - i), result, filter)
            addPointIfSatisfiesFilter(x - i, y - (radius - i), result, filter)
        }

        return result
    }

    private fun addPointIfSatisfiesFilter(
        x: Int,
        y: Int,
        result: MutableSet<Point>,
        filter: (x: Int, y: Int) -> Boolean
    ) {
        if (!filter(x, y)) {
            return
        }

        result.add(Point(x, y))
    }

    fun getTuningFrequency(): Long {
        return x.toLong() * 4_000_000 + y
    }
}

data class SensorArea(
    val sensor: Point,
    val beacon: Point,
    private val filter: (x: Int, y: Int) -> Boolean
) {
    private val radius = sensor.manhattanDistanceTo(beacon)
    val adjacentPoints = getAdjacentAreaPoints()
    private fun getAdjacentAreaPoints(): MutableSet<Point> {
        val adjacentCircleRadius = this.radius + 1
        return sensor.getPointsOnCircleWithRadius(adjacentCircleRadius, filter)
    }

    fun intersectsWith(other: SensorArea): Boolean {
        return sensor.manhattanDistanceTo(other.sensor) <= this.radius + other.radius + 1
    }

    fun contains(p: Point): Boolean {
        return sensor.manhattanDistanceTo(p) <= radius
    }
}

fun main() {
    val pairs = readInput()
    part1(pairs) //          6_078_701
    part2(pairs) // 12_567_351_400_528
}

private fun part1(pairs: List<Pair<Point, Point>>) {
    val yLine = 2_000_000
    val allBeacons = pairs.map { it.second }.toSet()

    val freePointsOnLine = pairs.map { (sensor, beacon) ->
        val closestPointOnLine = Point(sensor.x, yLine)

        val lineDistance = sensor.manhattanDistanceTo(closestPointOnLine)
        val beaconDistance = sensor.manhattanDistanceTo(beacon)

        // Ignore pairs long enough
        if (lineDistance > beaconDistance) {
            return@map setOf()
        }

        if (lineDistance == beaconDistance) {
            return@map setOf(closestPointOnLine)
        }

        val leftPointOnLine = Point(sensor.x - (beaconDistance - lineDistance), yLine)
        val rightPointOnLine = Point(sensor.x + (beaconDistance - lineDistance), yLine)

        return@map leftPointOnLine.lineTo(rightPointOnLine)
    }.toList().flatten().toSet()

    val excludeBeacons = freePointsOnLine.filterNot { it in allBeacons }

    println(excludeBeacons.count())
}

private fun part2(pairs: List<Pair<Point, Point>>) {
    val xRange = 0..4_000_000
    val yRange = 0..4_000_000

    val areas = pairs.parallelStream().map {
        SensorArea(it.first, it.second) { x, y -> x in xRange && y in yRange }
    }.toList()

    for (area in areas) {
        val intersectingAreas = areas
            .filterNot { it.sensor == area.sensor }
            .filter { it.intersectsWith(area) }

        for (a in intersectingAreas) {
            val toRemove = mutableSetOf<Point>()

            for (p in area.adjacentPoints) {
                if (a.contains(p)){
                    toRemove.add(p)
                }
            }

            area.adjacentPoints.removeAll(toRemove)
        }
    }

    val notCoveredPoints = areas
        .flatMap { it.adjacentPoints }
        .toSet()

    val result = notCoveredPoints.single().getTuningFrequency()
    println(result)
}

private fun readInput(): List<Pair<Point, Point>> {
    val r = "Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)".toRegex()
    val input = Util.readInputForTaskAsLines()

    return input.map { line ->
        val parsed = r.find(line)!!

        val sensorX = parsed.groupValues[1].toInt()
        val sensorY = parsed.groupValues[2].toInt()

        val beaconX = parsed.groupValues[3].toInt()
        val beaconY = parsed.groupValues[4].toInt()

        Pair(Point(sensorX, sensorY), Point(beaconX, beaconY))
    }
}
