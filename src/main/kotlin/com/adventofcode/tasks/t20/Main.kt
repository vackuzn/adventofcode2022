package com.adventofcode.tasks.t20

import com.adventofcode.Util
import java.util.LinkedList

data class Item(val originalIndex: Int, val value: Long) {
    fun getOffset(size: Int): Int {
        return value.mod(size - 1)
    }
}

fun main() {
    part1() //             2_622
    part2() // 1_538_773_034_088
}

private fun part1() {
    val lst = readItemsList()
    val field = LinkedList<Item>()
    field.addAll(lst)

    shuffle(lst, field)

    printCoords(lst, field)
}

private fun part2() {
    val decryptionKey = 811589153

    val lst = readItemsList()
    val field = LinkedList<Item>()
    field.addAll(lst.map { Item(it.originalIndex, it.value * decryptionKey) })

    repeat(10) {
        shuffle(lst, field)
    }

    printCoords(lst, field)
}

private fun shuffle(lst: List<Item>, field: LinkedList<Item>) {
    for (cmd in lst) {
        val itemIndex = field.indexOfFirst { it.originalIndex == cmd.originalIndex }
        val item = field[itemIndex]
        field.removeAt(itemIndex)

        val newIndex = getNewIndex(itemIndex, item.getOffset(lst.size), lst.size)
        field.add(newIndex, item)
    }
}

fun printCoords(lst: List<Item>, field: LinkedList<Item>) {
    val idxOfZero = field.indexOfFirst { it.value == 0L }

    val coord1 = field[getNthElementIndex(idxOfZero, 1000, lst.size)].value
    val coord2 = field[getNthElementIndex(idxOfZero, 2000, lst.size)].value
    val coord3 = field[getNthElementIndex(idxOfZero, 3000, lst.size)].value

    println(coord1 + coord2 + coord3)
}

private fun readItemsList(): List<Item> {
    val input = Util.readInputForTaskAsLines()
    return input.mapIndexed {idx, value -> Item(idx, value.toLong()) }
}

private fun getNewIndex(itemIndex: Int, offset: Int, listSize: Int): Int {
    var nonNegative = itemIndex + offset
    while (nonNegative < 0) {
        nonNegative += listSize - 1
    }

    val withinList = nonNegative.mod(listSize - 1)

    return if (withinList == 0) {
        listSize - 1
    } else {
        withinList
    }
}

private fun getNthElementIndex(itemIndex: Int, count: Int, listSize: Int): Int {
    return (itemIndex + count + 1).mod(listSize) - 1
}