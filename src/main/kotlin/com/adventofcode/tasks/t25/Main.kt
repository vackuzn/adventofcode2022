package com.adventofcode.tasks.t25

import com.adventofcode.Util
import kotlin.math.pow


fun main() {
    part1() // 33841257499180   2--2-0=--0--100-=210
}

fun part1() {
    val input = Util.readInputForTaskAsLines()
    val sum = input.sumOf { SnafuConverter.convertFrom(it) }
    val snafuSum = SnafuConverter.convertTo(sum)

    println("$sum   $snafuSum")
}

object SnafuConverter {
    fun convertTo(num: Long): String {
        val base5str = convertToBase5(num)

        var result = ""
        var overflowBitSet = false

        for (c in base5str.reversed()) {
            val digit = c.digitToInt() + if (overflowBitSet) 1 else 0

            overflowBitSet = digit > 2

            result = "${getSnafuCharForDigit(digit)}$result"
        }

        if (overflowBitSet) {
            result = "1$result"
        }

        return result
    }

    private fun getSnafuCharForDigit(d: Int): Char {
        return when(d) {
            0, 1, 2 -> d.digitToChar()
            3 -> '='
            4 -> '-'
            5 -> '0'
            else -> throw Exception("Unexpected $d")
        }
    }

    private fun convertToBase5(num: Long): String {
        var currentPower = 1
        var leftToProcess = num

        val digits = mutableListOf<Int>()
        while (leftToProcess != 0L) {
            val power = 5.0
                .pow(currentPower)
                .toLong()

            val previousPower = 5.0
                .pow(currentPower - 1)
                .toLong()

            val remainder = leftToProcess
                .mod(power)

            val digit = (remainder / previousPower).toInt()

            digits.add(digit)

            leftToProcess -= remainder
            currentPower++
        }

        val validation = digits
            .mapIndexed { idx, v -> v * 5.0.pow(idx).toLong() }
            .sum()

        assert(validation == num)

        return String(
            digits
            .map { it.digitToChar() }
            .toCharArray()
            .reversedArray()
        )
    }

    fun convertFrom(snafuNum: String): Long {
        return snafuNum
            .reversed()
            .mapIndexed { pos, c -> parseNum(c) * (5.0.pow(pos)).toLong() }
            .sum()
    }

    private fun parseNum(c: Char): Int {
        return when(c) {
            '=' -> -2
            '-' -> -1
            '0' -> 0
            '1' -> 1
            '2' -> 2
            else -> throw Exception("Unexpected char $c")
        }
    }
}