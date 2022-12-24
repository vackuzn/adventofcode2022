package com.adventofcode.tasks.t16

abstract class Strategy(protected val valves: List<Valve>) {
    private val logging = false

    private var cnt = 0
    fun printPossibleTargets(currentPos: Valve) {
        val possibleMoves = valves.filter { it.isActive }
        if (possibleMoves.isEmpty()) {
            return
        }

        val withDistance = possibleMoves
            .map { it to currentPos.getTravelTimeTo(it) }
            .sortedByDescending { it.first.flowRate }
            .toMap()

        if (logging) {
            println()
            println(++cnt)
            withDistance.map { (v, d) ->
                println("${v.name} flow rate: ${v.flowRate} distance $d")
            }
        }
    }
    fun getNextTarget(currentPos: Valve): Valve? {
        val res = getNextTargetInternal(currentPos)
        if (logging) {
            if (res == null) {
                println("No target is chosen")
            } else {
                println("Next target: ${res.name} flow rate ${res.flowRate} distance ${currentPos.getTravelTimeTo(res)}")
            }
        }
        return res
    }

    protected abstract fun getNextTargetInternal(currentPos: Valve): Valve?
}