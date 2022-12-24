package com.adventofcode.tasks.t16

class BalancedFlowDistanceStrategy(
    valves: List<Valve>,
    private val preselected: List<Int>
): Strategy(valves) {
    private val scoreFunction = { valve: Valve, distance: Int -> valve.flowRate.toDouble() / (distance * distance) }

    private var returned = 0

    override fun getNextTargetInternal(currentPos: Valve): Valve? {
        if (returned <= preselected.count() - 1) {
            val possibleMoves = valves.filter { it.isActive }.sortedBy { it.name }
            val result = possibleMoves[preselected[returned]]
            returned++
            return result
        }

        return smart(currentPos)
    }

    private fun smart(currentPos: Valve): Valve? {
        val possibleMoves = valves.filter { it.isActive }
        if (possibleMoves.isEmpty()) {
            return null
        }

        val withDistance = possibleMoves
            .map { it to currentPos.getTravelTimeTo(it) }
            .sortedByDescending { it.first.flowRate }
            .toMap()

        val withScores = withDistance.map { (valve, distance) ->
            valve to scoreFunction(valve, distance)
        }.sortedByDescending { it.second }.toMap()

        return withScores.keys.first()
    }
}