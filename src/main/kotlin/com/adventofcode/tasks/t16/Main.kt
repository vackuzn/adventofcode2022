package com.adventofcode.tasks.t16

import com.adventofcode.Util


data class Valve(
    val name: String,
    val flowRate: Int
) {
    val links = mutableSetOf<Valve>()

    fun addLink(target: Valve) {
        links.add(target)
    }
}

class Grid() {

}

fun main() {
    val data = loadData()
    val steps = 30

    for (step in 1..steps) {

    }


    val b = 3
}

private fun loadData(): MutableCollection<Valve> {
    val input = Util.readInputForTaskAsLines()
    val r = "Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? (.+)".toRegex()

    val valves = mutableMapOf<String, Valve>()

    for (line in input) {
        val result = r.find(line)!!

        val name = result.groupValues[1]
        val rate = result.groupValues[2].toInt()

        valves[name] = Valve(name, rate)
    }

    for (line in input) {
        val result = r.find(line)!!

        val name = result.groupValues[1]
        val valve = valves[name]!!

        result.groupValues[3]
            .split(",")
            .map { it.trim() }
            .map { valves[it]!! }
            .forEach { valve.addLink(it) }
    }

    return valves.values
}