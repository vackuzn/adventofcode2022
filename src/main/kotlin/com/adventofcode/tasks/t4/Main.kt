package com.adventofcode.tasks.t4

import com.adventofcode.Util

data class Assignment(val start: Int, val end: Int)

fun main() {
    val oneContainsAnother = false
    val input = Util.readInputForTaskAsLines()

    val assignmentPairs = input.map { readPairAssignment(it) }

    val overlappingPairs = assignmentPairs.filter {
        if (oneContainsAnother) {
            oneContainsAnother(it.first, it.second)
        } else {
            isOverlapping(it.first, it.second)
        }
    }

    println(overlappingPairs.count())
}

private fun readPairAssignment(line: String): Pair<Assignment, Assignment> {
    val pair = line.split(",")
        .map { a ->
            val startEnd = a
                .split("-")
                .map { it.toInt() }

            Assignment(startEnd.first(), startEnd.last())
        }

    return Pair(pair.first(), pair.last())
}

private fun oneContainsAnother(first: Assignment, second: Assignment): Boolean {
    val secondContainsFirst = first.start >= second.start && first.end <= second.end
    val firstContainsSecond = second.start >= first.start && second.end <= first.end

    return secondContainsFirst || firstContainsSecond
}

private fun isOverlapping(first: Assignment, second: Assignment): Boolean {
    val isNotOverlapping = first.end < second.start || second.end < first.start

    return !isNotOverlapping
}