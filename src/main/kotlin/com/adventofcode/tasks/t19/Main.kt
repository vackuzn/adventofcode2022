package com.adventofcode.tasks.t19

import com.adventofcode.Util
import kotlin.math.ceil

fun main() {
    part1(Util.readInputForTaskAsLines())            // 1418
    part2(Util.readInputForTaskAsLines().take(3))    // 4114
}

private fun part1(input: List<String>) {
    val timeLimit = 24
    val blueprints = loadBlueprints(input)

    val result = blueprints.sumOf { bp ->
        maxGeodesHarvested = 0
        val maxGeodesForBluePrint = play(bp, timeLimit)

        println("${bp.blueprintNumber} - $maxGeodesForBluePrint")
        bp.blueprintNumber * maxGeodesForBluePrint
    }

    println(result)
}

private fun part2(input: List<String>) {
    val timeLimit = 32
    val blueprints = loadBlueprints(input)

    val result = blueprints
        .map { bp ->
            maxGeodesHarvested = 0
            val maxGeodesForBluePrint = play(bp, timeLimit)
            println("${bp.blueprintNumber} - $maxGeodesForBluePrint")

            maxGeodesForBluePrint
        }
        .reduce { acc, i -> acc * i }

    println(result)
}

var maxGeodesHarvested = 0

private fun play(
    bp: Blueprint,
    minutesLeft: Int,
    gameState: GameState = GameState.createStartState()
): Int {
    if (minutesLeft < 0) {
        throw Exception("Minutes should be 0 or more")
    }

    if (!gameState.canOutperformCurrentLeader(minutesLeft)) {
        return gameState.getHarvestedGeodesCount()
    }

    if (minutesLeft == 0) {
        gameState.getHarvestedGeodesCount()
    }

    if (minutesLeft == 1) {
        return gameState
            .harvestResources(minutesLeft)
            .getHarvestedGeodesCount()
    }

    val robotsThatCanBeCreated = gameState
        .getPossibleRobotsToCreateInTime(bp, minutesLeft)


    if (robotsThatCanBeCreated.isEmpty()) {
        return gameState
            .harvestResources(minutesLeft)
            .getHarvestedGeodesCount()
    }

    return robotsThatCanBeCreated.map { (robotType, minutesRequired) ->
        val timeTakenForBuilding = minutesRequired + 1

        val nextState = gameState
            .harvestResources(timeTakenForBuilding)
            .buildRobot(bp, robotType)

        play(bp, minutesLeft - timeTakenForBuilding, nextState)
    }.max()
}

enum class ResourceType {
    Ore, Clay, Obsidian, Geode
}

data class GameState(
    private val robots: Map<ResourceType, Int>,
    private val resources: Map<ResourceType, Int>
) {
    fun canOutperformCurrentLeader(minutesLeft: Int): Boolean {
        val harvestedGeodes = resources[ResourceType.Geode] ?: 0
        val builtGeodeRobots = robots[ResourceType.Geode] ?: 0

        val theoreticalMaxGeodes = harvestedGeodes + builtGeodeRobots * minutesLeft + (1 until minutesLeft).sum()

        return theoreticalMaxGeodes >= maxGeodesHarvested
    }

    fun getPossibleRobotsToCreateInTime(bp: Blueprint, minutesLeft: Int): Map<ResourceType, Int> {
        if (minutesLeft <= 1) {
            return mapOf()
        }

        val robotsAvailableForResourceTypes = robots.filter { (_, count) -> count > 0 }.keys

        val newRobotCosts = bp
            .getRobotCosts(robotsAvailableForResourceTypes)
            // exclude what makes no sense
            .filter { (robotType, _) ->
                when (minutesLeft) {
                    2 -> robotType == ResourceType.Geode
                    else -> true
                }
            }
            // if already have enough robots for specific resource - exclude
            .filter { (robotType, _) ->
                bp.getMaxPrice(robotType) >= (robots[robotType] ?: 0)
            }

        val minutesPerRobot = newRobotCosts.map { (robotType, resourceReqs) ->
            robotType to resourceReqs.maxOf { (resType, resCount) -> minutesToHarvestEnoughResource(resType, resCount) }
        }.filter { (_, minutesToHarvest) -> minutesToHarvest < minutesLeft }.toMap()

        return minutesPerRobot
    }

    private fun minutesToHarvestEnoughResource(type: ResourceType, requestedAmount: Int): Int {
        val producerRobotsCount =
            robots[type] ?: throw Exception("At least one robot should be available to generate resource $type")
        val presentResourceCount = resources[type] ?: 0

        if (presentResourceCount >= requestedAmount) {
            return 0
        }

        val amountLeftToHarvest = requestedAmount - presentResourceCount
        return ceil(amountLeftToHarvest.toFloat() / producerRobotsCount).toInt()
    }

    fun harvestResources(minutes: Int): GameState {
        if (minutes == 0) {
            return this
        }

        val nextResources = resources.toMutableMap()

        robots.forEach { (robotType, robotCount) ->
            val currentResourceAmount = nextResources[robotType] ?: 0
            nextResources[robotType] = currentResourceAmount + minutes * robotCount
        }

        val geodesHarvested = nextResources[ResourceType.Geode] ?: 0
        if (geodesHarvested > maxGeodesHarvested) {
            maxGeodesHarvested = geodesHarvested
        }

        return copy(resources = nextResources)
    }

    fun getHarvestedGeodesCount(): Int {
        return resources[ResourceType.Geode] ?: 0
    }

    fun buildRobot(bp: Blueprint, robotType: ResourceType): GameState {
        val robotCount = robots[robotType] ?: 0
        val nextRobots = robots.toMutableMap()
        nextRobots[robotType] = robotCount + 1

        val nextResources = resources.toMutableMap()
        bp.getRobotCosts(robotType).forEach { (resType, price) ->
            nextResources[resType] = nextResources[resType]!! - price
        }

        return copy(robots = nextRobots, resources = nextResources)
    }

    companion object {
        fun createStartState(): GameState {
            return GameState(
                robots = mapOf(ResourceType.Ore to 1),
                resources = mapOf()
            )
        }
    }
}

private fun loadBlueprints(lines: List<String>): List<Blueprint> {
    val r = """Blueprint (\d+): Each ore robot costs (\d+) ore. Each clay robot costs (\d+) ore. Each obsidian robot costs (\d+) ore and (\d+) clay. Each geode robot costs (\d+) ore and (\d+) obsidian.""".toRegex()

    return lines.map { line ->
        val parsed = r.find(line)!!
        var idx = 0

        val blueprintNumber = parsed.groupValues[++idx].toInt()

        val oreRobotOreCost = parsed.groupValues[++idx].toInt()
        val clayRobotOreCost = parsed.groupValues[++idx].toInt()

        val obsidianRobotOreCost = parsed.groupValues[++idx].toInt()
        val obsidianRobotClayCost = parsed.groupValues[++idx].toInt()

        val geodeRobotOreCost = parsed.groupValues[++idx].toInt()
        val geodeRobotObsidianCost = parsed.groupValues[++idx].toInt()

        Blueprint(
            blueprintNumber,
            oreRobotOreCost,
            clayRobotOreCost,
            obsidianRobotOreCost,
            obsidianRobotClayCost,
            geodeRobotOreCost,
            geodeRobotObsidianCost
        )
    }
}

class Blueprint(
    val blueprintNumber: Int,
    oreRobotOreCost: Int,
    clayRobotOreCost: Int,
    obsidianRobotOreCost: Int,
    obsidianRobotClayCost: Int,
    geodeRobotOreCost: Int,
    geodeRobotObsidianCost: Int
) {
    private val robotCosts = listOf(
        ResourceType.Ore to mapOf(ResourceType.Ore to oreRobotOreCost),
        ResourceType.Clay to mapOf(ResourceType.Ore to clayRobotOreCost),
        ResourceType.Obsidian to mapOf(
            ResourceType.Ore to obsidianRobotOreCost,
            ResourceType.Clay to obsidianRobotClayCost
        ),
        ResourceType.Geode to mapOf(
            ResourceType.Ore to geodeRobotOreCost,
            ResourceType.Obsidian to geodeRobotObsidianCost
        )
    ).toMap()

    private val maxResourceConsumption = robotCosts
        .values
        .map { it.toList() }
        .flatten()
        .groupBy { it.first }
        .map { (type, costs) -> type to costs.maxOf { it.second } }
        .toMap()

    fun getRobotCosts(robotType: ResourceType): Map<ResourceType, Int> {
        return robotCosts[robotType]!!
    }

    fun getRobotCosts(availableResources: Set<ResourceType>): Map<ResourceType, Map<ResourceType, Int>> {
        return robotCosts
            .filter { (_, requirements) ->
                requirements.all { (resType, _) -> resType in availableResources }
            }.toMap()
    }

    fun getMaxPrice(type: ResourceType): Int {
        return maxResourceConsumption[type] ?: Int.MAX_VALUE
    }
}