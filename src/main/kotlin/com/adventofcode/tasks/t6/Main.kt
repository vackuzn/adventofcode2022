package com.adventofcode.tasks.t6

import com.adventofcode.Util


fun main() {
    val signal = Util.readInputForTaskAsLines().first()

    println(getStartSignalIdx(signal))
    println(getStartOfTheMessageIdx(signal))
}

private fun getStartSignalIdx(signal: String): Int {
    return getIndexOfFirstDistinctSequenceOfLength(signal, 4)
}

private fun getStartOfTheMessageIdx(signal: String): Int {
    return getIndexOfFirstDistinctSequenceOfLength(signal, 14)
}

private fun getIndexOfFirstDistinctSequenceOfLength(str: String, distinctCount: Int): Int {
    assert(distinctCount > 0)

    var idx = distinctCount
    while (true) {
        if (isDistinct(str, distinctCount, idx)) {
            return idx
        }

        idx++
    }
}

private fun isDistinct(signal: String, distinctCount: Int, idx: Int): Boolean {
    val start = idx - distinctCount

    val candidateSequence = signal.substring(start, idx)
    return candidateSequence.map { it }.distinct().count() == distinctCount
}