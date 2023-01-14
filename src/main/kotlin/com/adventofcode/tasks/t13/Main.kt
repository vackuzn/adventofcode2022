package com.adventofcode.tasks.t13

import com.adventofcode.Util


fun main() {
    part1() //  5_905
    part2() // 21_691
}

fun part1() {
    val input = Util.readInputForTaskAsLines()

    var successPairs = 0
    var pairIndex = 0

    for (idx in input.indices step 3) {
        pairIndex++

        val left = InputParser.parse(input[idx])
        val right = InputParser.parse(input[idx + 1])

        val res = left.compareTo(right)
        if (res == -1) {
            successPairs += pairIndex
        }
    }

    println(successPairs)
}

fun part2() {
    val firstKey = InputParser.parse("[[2]]")
    val secondKey = InputParser.parse("[[6]]")

    val items = Util.readInputForTaskAsLines()
        .filterNot { it.isEmpty() }
        .map { InputParser.parse(it) } + listOf(firstKey, secondKey)

    val sorted = items.sorted()

    val firstKeyIndex = sorted.indexOf(firstKey) + 1
    val secondKeyIndex = sorted.indexOf(secondKey) + 1

    println(firstKeyIndex * secondKeyIndex)
}

interface Element : Comparable<Element>

class ValueElement(val value: Int) : Element {
    override fun compareTo(other: Element): Int {
        if (other is ValueElement) {
            return value.compareTo(other.value)
        }

        return ListElement(this).compareTo(other)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Element) {
            return compareTo(other) == 0
        }

        return false
    }
}

data class ListElement(private val childElements: List<Element>) : Element {
    constructor(el: Element): this(listOf(el))

    override fun compareTo(other: Element): Int {
        if (other is ListElement) {
            return compareTo(other)
        }

        return compareTo(ListElement(other))
    }

    private fun compareTo(other: ListElement): Int {
        for (idx in childElements.indices) {
            if (idx !in other.childElements.indices) {
                return 1
            }

            val l = childElements[idx]
            val r = other.childElements[idx]

            val elementCompareRes = l.compareTo(r)
            if (elementCompareRes != 0) {
                return elementCompareRes
            }
        }

        return if (childElements.count() < other.childElements.count()) {
            -1
        } else {
            0
        }
    }

    override fun hashCode(): Int {
        return childElements.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Element) {
            return compareTo(other) == 0
        }

        return false
    }
}


class InputParser private constructor(private val line: String) {
    companion object {
        fun parse(line: String): ListElement {
            val parser = InputParser(line)

            return parser.parseListElement()
        }
    }

    var pos = 0

    private fun parseListElement(): ListElement {
        val items = mutableListOf<Element>()

        while (true) {
            pos++

            when(line[pos]) {
                '[' -> {
                    val nestedList = parseListElement()
                    items.add(nestedList)
                }
                ']' -> {
                    break
                }
                ',' -> { }
                else -> {
                    val intItem = ValueElement(readInt())
                    items.add(intItem)
                }
            }
        }

        return ListElement(items)
    }

    private fun readInt(): Int {
        var numberSymbols = ""

        while (line[pos].isDigit()) {
            numberSymbols += line[pos]
            pos++
        }

        pos--

        return numberSymbols.toInt()
    }
}