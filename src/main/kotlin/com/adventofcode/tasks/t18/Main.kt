package com.adventofcode.tasks.t18

import com.adventofcode.Util


data class Unit(val x: Int, val y: Int, val z: Int) {
    fun down() = this.copy(z = this.z - 1)
    fun up() = this.copy(z = this.z + 1)

    fun backward() = this.copy(y = this.y - 1)
    fun forward() = this.copy(y = this.y + 1)


    fun left() = this.copy(x = this.x - 1)
    fun right() = this.copy(x = this.x + 1)
}

data class SurroundingBox(private val units: Set<Unit>) {
    private val xMin: Int
    private val xMax: Int

    private val yMin: Int
    private val yMax: Int

    private val zMin: Int
    private val zMax: Int

    private val notBubbleUnits = mutableSetOf<Unit>()
    private val bubbleUnits = mutableSetOf<Unit>()

    init {
        val xBorders = units.map { it.x }.sorted()
        xMin = xBorders.first()
        xMax = xBorders.last()

        val yBorders = units.map { it.y }.sorted()
        yMin = yBorders.first()
        yMax = yBorders.last()

        val zBorders = units.map { it.z }.sorted()
        zMin = zBorders.first()
        zMax = zBorders.last()
    }

    private fun isWithinBox(unit: Unit): Boolean {
        val xWithinBorders = unit.x in xMin..xMax
        val yWithinBorders = unit.y in yMin..yMax
        val zWithinBorders = unit.z in zMin..zMax

        return xWithinBorders && yWithinBorders && zWithinBorders
    }

    fun findAdjacentBubble(u: Unit) {
        if (!units.contains(u.down())) {
            analyzeIfBubble(u.down())
        }
        if (!units.contains(u.up())) {
            analyzeIfBubble(u.up())
        }

        if (!units.contains(u.backward())) {
            analyzeIfBubble(u.backward())
        }
        if (!units.contains(u.forward())) {
            analyzeIfBubble(u.forward())
        }

        if (!units.contains(u.left())) {
            analyzeIfBubble(u.left())
        }
        if (!units.contains(u.right())) {
            analyzeIfBubble(u.right())
        }
    }

    private fun analyzeIfBubble(u: Unit): Boolean {
        if (bubbleUnits.contains(u)) {
            return true
        }

        if (notBubbleUnits.contains(u)) {
            return false
        }

        val analyzedArea = mutableSetOf<Unit>()

        return when (analyzeIfBubble(u, analyzedArea)) {
            null -> {
                bubbleUnits.addAll(analyzedArea)
                true
            }
            true -> {
                bubbleUnits.addAll(analyzedArea)
                true
            }
            false -> {
                notBubbleUnits.addAll(analyzedArea)
                false
            }
        }
    }

    private fun analyzeIfBubble(u: Unit, analyzedArea: MutableSet<Unit>): Boolean? {
        if (analyzedArea.contains(u)) {
            return null
        }

        // add to non bubble set, stop
        if (!isWithinBox(u)) {
            return false
        }

        if (notBubbleUnits.contains(u)) {
            return false
        }

        // reached other bubble points
        if (bubbleUnits.contains(u)) {
            return true
        }

        // not bubble anymore, stop
        if (units.contains(u)) {
            return null
        }

        analyzedArea.add(u)

        val up = analyzeIfBubble(u.up(), analyzedArea)
        val down = analyzeIfBubble(u.down(), analyzedArea)
        val left = analyzeIfBubble(u.left(), analyzedArea)
        val right = analyzeIfBubble(u.right(), analyzedArea)
        val fwd = analyzeIfBubble(u.forward(), analyzedArea)
        val bwd = analyzeIfBubble(u.backward(), analyzedArea)

        return up
            ?: down
            ?: left
            ?: right
            ?: fwd
            ?: bwd
    }

    fun isBubble(unit: Unit): Boolean {
        return bubbleUnits.contains(unit)
    }
}

fun main() {
    part1() // 4418
    part2() // 2486
}

private fun part1() {
    val units = readUnits()
    var totalSpace = 0

    for (u in units) {
        var space = 6

        if (units.contains(u.down())) {
            space--
        }
        if (units.contains(u.up())) {
            space--
        }

        if (units.contains(u.backward())) {
            space--
        }
        if (units.contains(u.forward())) {
            space--
        }

        if (units.contains(u.left())) {
            space--
        }
        if (units.contains(u.right())) {
            space--
        }

        totalSpace += space
    }

    println(totalSpace)
}

private fun part2() {
    val units = readUnits()
    val box = SurroundingBox(units)

    for (u in units) {
        box.findAdjacentBubble(u)
    }

    var totalSpace = 0

    for (u in units) {
        var space = 0

        if (!units.contains(u.down()) && !box.isBubble(u.down())) {
            space++
        }
        if (!units.contains(u.up()) && !box.isBubble(u.up())) {
            space++
        }

        if (!units.contains(u.backward()) && !box.isBubble(u.backward())) {
            space++
        }
        if (!units.contains(u.forward()) && !box.isBubble(u.forward())) {
            space++
        }

        if (!units.contains(u.left()) && !box.isBubble(u.left())) {
            space++
        }
        if (!units.contains(u.right()) && !box.isBubble(u.right())) {
            space++
        }

        totalSpace += space
    }

    println(totalSpace)
}

private fun readUnits(): Set<Unit> {
    val input = Util.readInputForTaskAsLines()

    return input.map {
        val a = it.split(",")

        val x = a[0].toInt()
        val y = a[1].toInt()
        val z = a[2].toInt()

        Unit(x, y, z)
    }.toSet()
}

