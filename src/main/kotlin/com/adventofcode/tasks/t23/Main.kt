package com.adventofcode.tasks.t23

import com.adventofcode.Util


fun main() {
    part1() // 3_684
    part2() //   862
}

fun part1() {
    val field = loadField()
    val directionEnumerator = DirectionEnumerator()

    // for each round:
    repeat(10) {
        makeMove(field, directionEnumerator)
    }

    val score = field.calculateScore()
    println(score)
}

fun part2() {
    val field = loadField()
    val directionEnumerator = DirectionEnumerator()

    // for each round:
    var cnt = 1
    while (makeMove(field, directionEnumerator)) { cnt++ }

    println(cnt)
}

private fun makeMove(field: Field, directionEnumerator: DirectionEnumerator): Boolean {
    val move = mutableMapOf<Point, Point>()

    // 1) prepare next move:
    field.elves.forEach { elven ->
        // 1.1. No adjacent elf -> no move
        if (!field.hasAdjacentElves(elven)) {
            return@forEach
        }

        // 1.2. Check all directions(directions) -> choose move
        // 1.3. All directions blocked -> no move
        val direction = directionEnumerator
            .getDirections()
            .firstOrNull { d -> elven.isDirectionFree(d, field.elves) } ?: return@forEach

        val target = elven.moveTo(direction)
        move[elven] = target
    }

    // 2. Cancel moves to the same cell (map [to -> list<from>]m if more than 1 - skip both moves
    val r = mutableMapOf<Point, MutableList<Point>>()
    move.forEach { (from, to) ->
        if (r[to] == null) {
            r[to] = mutableListOf(from)
        } else {
            r[to]!!.add(from)
        }
    }

    r.values
        .filter { listFrom -> listFrom.count() > 1 }
        .forEach { listFrom -> listFrom.forEach { f -> move.remove(f) } }

    // 3. Make moves (replace from -> to)
    if (move.isEmpty()) {
        return false
    }

    field.makeMove(move)

    directionEnumerator.nextMove()

    return true
}

class Field(val elves: MutableSet<Point>) {
    fun hasAdjacentElves(elven: Point): Boolean {
        return elven.getAdjacentPoints().any { elves.contains(it)}
    }

    fun makeMove(move: Map<Point, Point>) {
        move.forEach { (from, to) ->
            elves.remove(from)
            elves.add(to)
        }
    }

    fun calculateScore(): Int {
        val yCoords = elves.map { it.y }
        val xCoords = elves.map { it.x }

        val topIdx = yCoords.max()
        val bottomIdx = yCoords.min()
        val rightIdx = xCoords.max()
        val leftIdx = xCoords.min()

        val height = topIdx - bottomIdx + 1
        val width = rightIdx - leftIdx + 1

        return height * width - elves.count()
    }
}

fun loadField(): Field {
    val input = Util.readInputForTaskAsLines()

    val elves = mutableSetOf<Point>()
    input.forEachIndexed { y, line ->
        line.forEachIndexed { x, c ->
            if (c == '#') {
                elves.add(Point(x, y))
            }
        }
    }

    return Field(elves)
}

enum class Direction(val idx: Int) {
    North(0),
    South(1),
    West(2),
    East(3);

    companion object {
        val count = Direction.values().count()

        private val map = Direction
            .values()
            .associateBy { it.idx }
        infix fun from(value: Int) = map[value]!!
    }
}

/*
If there is no Elf in the N, NE, or NW adjacent positions, the Elf proposes moving north one step.
If there is no Elf in the S, SE, or SW adjacent positions, the Elf proposes moving south one step.
If there is no Elf in the W, NW, or SW adjacent positions, the Elf proposes moving west one step.
If there is no Elf in the E, NE, or SE adjacent positions, the Elf proposes moving east one step.
 */

class DirectionEnumerator {
    private var offset = 0
    fun nextMove() {
        offset = (offset + 1).mod(Direction.count)
    }
    fun getDirections(): List<Direction> {
        return (0..Direction.count * 2)
            .drop(offset)
            .take(Direction.count)
            .map { it.mod(Direction.count) }
            .map { Direction.from(it) }
    }
}

data class Point(val x: Int, val y: Int) {
    fun getAdjacentPoints(): List<Point> {
        return listOf(
            copy(x = x - 1, y = y + 1),
            copy(y = y + 1),
            copy(x = x + 1, y = y + 1),

            copy(x = x - 1),
            copy(x = x + 1),

            copy(x = x - 1, y = y - 1),
            copy(y = y - 1),
            copy(x = x + 1, y = y - 1),
        )
    }

    fun isDirectionFree(d: Direction, elves: MutableSet<Point>): Boolean {
        val targets = when(d) {
            Direction.North -> listOf(
                copy(x = x - 1, y = y - 1),
                copy(y = y - 1),
                copy(x = x + 1, y = y - 1)
            )
            Direction.South -> listOf(
                copy(x = x - 1, y = y + 1),
                copy(y = y + 1),
                copy(x = x + 1, y = y + 1)
            )
            Direction.West -> listOf(
                copy(x = x - 1, y = y + 1),
                copy(x = x - 1),
                copy(x = x - 1, y = y - 1)
            )
            Direction.East -> listOf(
                copy(x = x + 1, y = y + 1),
                copy(x = x + 1),
                copy(x = x + 1, y = y - 1)
            )
        }

        return !targets.any { elves.contains(it) }
    }

    fun moveTo(d: Direction): Point {
        return when(d) {
            Direction.North -> copy(y = y - 1)
            Direction.South -> copy(y = y + 1)
            Direction.West -> copy(x = x - 1)
            Direction.East -> copy(x = x + 1)
        }
    }
}