package com.adventofcode.tasks.t1

import com.adventofcode.Util

fun main() {
    val data = loadData()

    val result = data.map { it.sum() }.sortedDescending().take(3).sum()

    println(result)
}

fun loadData(): List<List<Int>> {
    val input = Util.readInputForTaskAsLines()

    return sequence {
        val result = mutableListOf<Int>()
        input.forEach { line ->
            if (line.isEmpty()) {
                yield(result.toList())
                result.clear()
            } else {
                val num = line.toInt()
                result.add(num)
            }
        }

        yield(result.toList())
    }.toList()
}