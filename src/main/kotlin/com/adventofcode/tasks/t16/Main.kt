package com.adventofcode.tasks.t16

import com.adventofcode.Util
import java.util.concurrent.Executors


data class Valve(
    val name: String,
    val flowRate: Int
) {
    var isActive = flowRate > 0
    private val links = mutableSetOf<Valve>()

    fun addLink(target: Valve) {
        links.add(target)
    }

    fun getTravelTimeTo(target: Valve): Int {
        if (target == this) {
            return 0
        }

        val visited = mutableSetOf<Valve>()
        val path = getPathTo(target, visited)!!

        return path.count() - 1
    }

    private fun getPathTo(target: Valve, visited: MutableSet<Valve>): List<Valve>? {
        visited.add(this)

        val tmp = mutableListOf<List<Valve>>()

        for (link in links.filterNot { visited.contains(it) }) {
            if (link == target) {
                return listOf(this, link)
            }

            val shortestPathFromLink = link.getPathTo(target, visited.toMutableSet())
            if (shortestPathFromLink != null) {
                tmp.add(shortestPathFromLink)
            }
        }

        val shortestPathFromLink = tmp.minByOrNull { it.count() }

        return if (shortestPathFromLink != null) {
            listOf(this) + shortestPathFromLink
        } else {
            null
        }
    }

    fun openValve() {
        isActive = false
    }
}

fun main() {
//    bruteForce()

    val valves = loadData()
    val strategy = BalancedFlowDistanceStrategy(valves, listOf(6, 4, 10))
    val game = Game(strategy, valves.first { it.name == "AA" })

    game.play()

    println(game.score)
}

private fun bruteForce() {
    val workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    val startPos = 14

    val result = mutableMapOf<List<Int>, Int>()

    val inputData = sequence {
        for (p1 in 0..startPos) {
            for (p2 in 0 until startPos) {
                for (p3 in 0..startPos - 2) {
                    yield(listOf(p1, p2, p3))
                }
            }
        }
    }.toList()

    inputData.map { input ->
        workerPool.submit {
            val score = bruteForceRun(input)
            add(result, input, score)
        }
    }.forEach { it.get() }

    //0 = {Pair@1347} ([6, 4, 10], 1421)
    //1 = {Pair@1348} ([6, 4, 1], 1382)
    //2 = {Pair@1349} ([6, 1, 3], 1362)
    //3 = {Pair@1350} ([4, 5, 1], 1312)
    //4 = {Pair@1351} ([5, 4, 10], 1310)
    //5 = {Pair@1352} ([5, 11, 4], 1298)

    val a = 5
}

@Synchronized
private fun add(result: MutableMap<List<Int>, Int>, input: List<Int>, score: Int) {
    result[input] = score
}

private fun bruteForceRun(take: List<Int>): Int {
    val valves = loadData()
    val strategy = BruteForceDistanceStrategy(valves, take)

    val game = Game(strategy, valves.first { it.name == "AA" })

    game.play()

    return game.score
}

fun createStrategy(valves: List<Valve>): Strategy {
    return BalancedFlowDistanceStrategy(valves, listOf(6, 4, 10))
}


class Game(
    private val strategy: Strategy,
    start: Valve
) {
    var currentPos = start
    var minutesLeft = 30
    var score = 0

    fun play() {
        while (minutesLeft >= 0) {
            strategy.printPossibleTargets(currentPos)
            val nextTarget = strategy.getNextTarget(currentPos) ?: return

            val travelTime = currentPos.getTravelTimeTo(nextTarget)
            if (minutesLeft <= travelTime) {
                return
            }

            makeMoveAndOpenValve(nextTarget, travelTime)

            //println("Open ${nextTarget.name} on minute ${30 - minutesLeft}")
        }
    }

    private fun makeMoveAndOpenValve(nextTarget: Valve, travelTime: Int) {
        minutesLeft -= travelTime + 1
        currentPos = nextTarget
        score += nextTarget.flowRate * minutesLeft
        nextTarget.openValve()
    }
}

private fun loadData(): List<Valve> {
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

    return valves.values.toList()
}