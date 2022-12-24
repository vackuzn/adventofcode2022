package com.adventofcode.tasks.t16

class HardcodedStrategy(valves: List<Valve>): Strategy(valves) {
    private val elements = listOf(
        valves.first { it.name == "DD" },
        valves.first { it.name == "BB" },
        valves.first { it.name == "JJ" },
        valves.first { it.name == "HH" },
        valves.first { it.name == "EE" },
        valves.first { it.name == "CC" }
    ).iterator()

    override fun getNextTargetInternal(currentPos: Valve): Valve? {
        if (elements.hasNext()) {
            return elements.next()
        }

        return null
    }
}