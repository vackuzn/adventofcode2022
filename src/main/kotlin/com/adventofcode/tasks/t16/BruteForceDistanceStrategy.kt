package com.adventofcode.tasks.t16

class BruteForceDistanceStrategy(
    valves: List<Valve>,
    private val take: List<Int>
): Strategy(valves) {
    private var returned = 0

    override fun getNextTargetInternal(currentPos: Valve): Valve? {
        val possibleMoves = valves.filter { it.isActive }.sortedBy { it.name }

        if (possibleMoves.isEmpty()) {
            return null
        }

        if (take.size <= returned) {
            return null
        }

        val result = possibleMoves[take[returned]]
        returned++

        return result
    }
}