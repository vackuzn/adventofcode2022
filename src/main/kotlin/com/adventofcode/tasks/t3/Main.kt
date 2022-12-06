package com.adventofcode.tasks.t3

import com.adventofcode.Util

fun main() {
    val groupOf3 = true
    val input = Util.readInputForTaskAsLines()

    val result = if (groupOf3) getSumOfGroupOf3(input) else getSumOfCompartmentItems(input)

    println(result)
}

fun getSumOfGroupOf3(input: List<String>): Int {
    var result = 0

    for (current in input.indices step 3) {
        val elf1 = getDistinctContent(input[current])
        val elf2 = getDistinctContent(input[current + 1])
        val elf3 = getDistinctContent(input[current + 2])

        val tokenItem = elf1.filter { elf2.contains(it) }.single { elf3.contains(it) }
        result += tokenItem
    }

    return result
}

fun getSumOfCompartmentItems(input: List<String>): Int {
    return input.sumOf { line ->
        getCommonItem(line)
    }
}

fun getCommonItem(contents: String): Int {
    val compartment1 = getDistinctContent(contents.substring(0 until (contents.length / 2)))
    val compartment2 = getDistinctContent(contents.substring(contents.length / 2))

    return compartment1.single { item1 -> compartment2.contains(item1) }
}

private fun getDistinctContent(contents: String): List<Int> {
    return contents.map { getItemScore(it) }.distinct()
}

private fun getItemScore(item: Char): Int {
    return if (item.isUpperCase()) {
        item.code - 38
    } else {
        item.code - 96
    }
}