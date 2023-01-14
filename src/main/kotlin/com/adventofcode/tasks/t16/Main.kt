package com.adventofcode.tasks.t16

import com.adventofcode.Util

fun main() {
    part1() // 1947
    part2() // 2556
}

private fun part1() {
    val startRoomName = "AA"
    val minutes = 30

    // Load input
    val rooms = loadData()

    // Calculate the shortest path between each room to all other rooms
    val shortestPaths = calculateShortestPaths(rooms)

    // traverse all paths recursively until timer runs out
    val result = findBestScore(rooms[startRoomName]!!, shortestPaths, minutes)
    println(result)
}

private fun part2() {
    val startRoomName = "AA"
    val minutes = 26

    // Load input
    val rooms = loadData()

    // Calculate the shortest path between each room with start room and flow > 0
    val shortestPaths = calculateShortestPaths(rooms)

    // We have 2 actors, so we don't want them to chase the same rooms.
    // We generate all possible combinations size = half of non-zero flow rooms, run search for each of them and find max result
    val roomsWithNonZeroFlow = shortestPaths.keys.filter { it.flowRate > 0 }.sortedBy { it.name }
    val combinationsOfRooms = Utils.generateCombinations(roomsWithNonZeroFlow, roomsWithNonZeroFlow.size / 2)

    val scoreForTheBestGroupCombination = combinationsOfRooms.map { halfOfRooms ->
        val otherHalfOfRooms = roomsWithNonZeroFlow.filterNot { it in halfOfRooms }.toSet()

        val score = findBestScore(
            from = rooms[startRoomName]!!,
            shortestPaths = shortestPaths,
            minutesLeft = minutes,
            visited = halfOfRooms
        ) + findBestScore(
            from = rooms[startRoomName]!!,
            shortestPaths = shortestPaths,
            minutesLeft = minutes,
            visited = otherHalfOfRooms
        )

        score
    }.max()

    println(scoreForTheBestGroupCombination)
}

private fun loadData(): Map<String, ValveRoom> {
    val r = "Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? (.+)".toRegex()

    val input = Util.readInputForTaskAsLines()
    val valveRooms = mutableMapOf<String, ValveRoom>()

    for (line in input) {
        val parsed = r.find(line)!!

        val name = parsed.groupValues[1]
        val rate = parsed.groupValues[2].toInt()
        val directLinkRooms = parsed
            .groupValues[3]
            .split(",")
            .map { it.trim() }

        valveRooms[name] = ValveRoom(name, rate, directLinkRooms)
    }

    return valveRooms
}

private fun calculateShortestPaths(rooms: Map<String, ValveRoom>): Map<ValveRoom, Map<ValveRoom, Int>> {
    val pathDoesntExistValue = 50_000

    val shortestPaths = rooms.values.associate {
        it.name to it.directLinkRooms.associateWith { 1 }.toMutableMap()
    }.toMutableMap()

    fun getPathCost(from: String, to: String): Int {
        val costs = shortestPaths[from]!!
        return costs[to] ?: pathDoesntExistValue
    }

    fun setPathCost(from: String, to: String, cost: Int) {
        val costs = shortestPaths[from]!!
        costs[to] = cost
    }

    // correct order: waypoint, from, to; wrong order: start, waypoint, finish
    // In case of wrong order, operation has to be done twice, as some nodes are not populated on first run
    Utils.generatePermutations(rooms.keys, 3).forEach { (waypoint, start, finish) ->
        val directPathCost = getPathCost(start, finish)
        val pathWithLayoverCost = getPathCost(start, waypoint) + getPathCost(waypoint, finish)

        setPathCost(start, finish, minOf(directPathCost, pathWithLayoverCost))
    }

    // Filter out rooms with zero flow, as we will not target them
    val startRoomAndRoomsWithPositiveFlow = shortestPaths
        .filter { (name, _) -> name in rooms.values.filter { it.flowRate > 0 || it.name == "AA" }.map { it.name } }
        .map { (name, paths) ->
            name to paths.filter { (roomName, _) ->
                roomName in rooms.values.filter { it.flowRate > 0 }.map { it.name }
            }.toMap()
        }.toMap()

    // Transform room names to objects
    return startRoomAndRoomsWithPositiveFlow.map { (roomName, shortestPaths) ->
        rooms[roomName]!! to shortestPaths.map { (targetRoomName, travelTime) -> rooms[targetRoomName]!! to travelTime }
            .toMap()
    }.toMap()
}

// This method would have had n! complexity, in provided data it's 15! - dead end.
// Luckily, we have time limitation. We stop once we're out of time, so complexity remains reasonable
private fun findBestScore(
    from: ValveRoom,
    shortestPaths: Map<ValveRoom, Map<ValveRoom, Int>>,
    minutesLeft: Int,
    visited: Set<ValveRoom> = setOf(),
    currentScore: Int = 0
): Int {
    val potentialTargets = shortestPaths[from]!!
        .filter { (room, _) -> room.flowRate > 0 }              // Take only rooms with non-zero valves
        .filter { (_, travelTime) -> travelTime < minutesLeft } // Rooms that we have enough time for
        .filterNot { (room, _) -> room in visited }             // Don't go back

    val maxScore = potentialTargets.map { (nextRoom, travelTime) ->
        val minutesLeftAtNextMove = minutesLeft - (travelTime + 1)
        val scoreAtNextMove = currentScore + minutesLeftAtNextMove * nextRoom.flowRate

        findBestScore(
            from = nextRoom,
            shortestPaths = shortestPaths,
            minutesLeft = minutesLeftAtNextMove,
            visited = visited + nextRoom,
            currentScore = scoreAtNextMove
        )
    }.maxOrNull()

    return maxScore ?: currentScore
}

data class ValveRoom(
    val name: String,
    val flowRate: Int,
    val directLinkRooms: List<String>
)

object Utils {
    fun <T> generatePermutations(
        items: Collection<T>,
        depth: Int,
        filtered: List<T> = listOf()
    ): Sequence<List<T>> {
        return sequence {
            items
                .filterNot { it in filtered }
                .forEach { item ->
                    if (depth > 1) {
                        generatePermutations(items, depth - 1, filtered + item).forEach {
                            yield(listOf(item) + it)
                        }
                    } else {
                        yield(listOf(item))
                    }
                }
        }
    }

    fun <T> generateCombinations(items: List<T>, combinationSize: Int): Sequence<Set<T>> {
        return sequence {
            items
                .dropLast(combinationSize - 1)
                .forEachIndexed { idx, item ->
                    if (combinationSize > 1) {
                        generateCombinations(items.drop(idx + 1), combinationSize - 1).forEach { child ->
                            yield(setOf(item) + child)
                        }
                    } else {
                        yield(setOf(item))
                    }
                }
        }
    }
}