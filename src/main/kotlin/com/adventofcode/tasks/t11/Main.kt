package com.adventofcode.tasks.t11


class Monkey(
    startItems: List<Long>,
    private val testDivisibleBy: Int,
    private val targetIfTrue: Int,
    private val targetIfFalse: Int,
    private val decreaseWorryLevelOp: (worryLevel: Long) -> Long,
    private val increaseWorryLevelOp: (worryLevel: Long) -> Long
) {
    private val currentItems: MutableList<Long> = mutableListOf()
    var thrownItemsCount: Long = 0

    init {
        currentItems.addAll(startItems)
    }

    fun throwAllItems(allMonkeys: List<Monkey>) {
        currentItems.forEach { item ->
            val updatedItem = getNewWorryLevel(item)

            val targetMonkey = if (updatedItem.mod(testDivisibleBy) == 0) {
                allMonkeys[targetIfTrue]
            } else {
                allMonkeys[targetIfFalse]
            }

            throwItemAt(updatedItem, targetMonkey)

            thrownItemsCount++
        }

        currentItems.clear()
    }

    private fun throwItemAt(updatedItem: Long, target: Monkey) {
        target.catchItem(updatedItem)
    }

    private fun catchItem(updatedItem: Long) {
        currentItems.add(updatedItem)
    }

    private fun getNewWorryLevel(item: Long): Long {
        val worryIncreased = increaseWorryLevelOp(item)
        return decreaseWorryLevelOp(worryIncreased)
    }
}

fun main() {
    part1() //         72_884
    part2() // 15_310_845_153
}

private fun part1() {
    val monkeys = getInput(false)

    repeat(20) {
        monkeys.forEach { monkey ->
            monkey.throwAllItems(monkeys)
        }
    }

    printMonkeyBusinessScore(monkeys)
}

private fun part2() {
    val monkeys = getInput(true)

    repeat(10_000) {
        monkeys.forEach { monkey ->
            monkey.throwAllItems(monkeys)
        }
    }

    printMonkeyBusinessScore(monkeys)
}


fun printMonkeyBusinessScore(monkeys: List<Monkey>) {
    val monkeyBusinessScore = monkeys
        .map { it.thrownItemsCount }
        .sortedDescending()
        .take(2)
        .reduce { acc, i ->  acc * i }

    println(monkeyBusinessScore)
}

fun getInput(isPart2: Boolean): List<Monkey> {
    val divisors = listOf(2, 13, 3, 17, 19, 7, 11, 5) // 2 3 5 7 11 17 19
    val greatestCommonDivisor = divisors.reduce { acc, i ->  acc * i }.toLong()

    val reduceWorryFunction = if (isPart2) {
        { item: Long -> item.mod(greatestCommonDivisor)}
    } else {
        { item: Long -> item / 3 }
    }

    return listOf(
        Monkey(listOf(89, 95, 92, 64, 87, 68), divisors[0],7,4, reduceWorryFunction) { it * 11 },
        Monkey(listOf(87, 67), divisors[1],3,6, reduceWorryFunction) { it + 1 },
        Monkey(listOf(95, 79, 92, 82, 60), divisors[2],1,6, reduceWorryFunction) { it + 6 },
        Monkey(listOf(67, 97, 56), divisors[3],7,0, reduceWorryFunction) { it * it },
        Monkey(listOf(80, 68, 87, 94, 61, 59, 50, 68), divisors[4],5,2, reduceWorryFunction) { it * 7 },
        Monkey(listOf(73, 51, 76, 59), divisors[5],2,1, reduceWorryFunction) { it + 8 },
        Monkey(listOf(92), divisors[6],3,0, reduceWorryFunction) { it + 5 },
        Monkey(listOf(99, 76, 78, 76, 79, 90, 89), divisors[7],4,5, reduceWorryFunction) { it + 7 }
    )
}
