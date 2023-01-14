package com.adventofcode.tasks.t5

import com.adventofcode.Util
import java.util.Stack


data class MoveCommand(
    val moveCount: Int,
    val moveFrom: Int,
    val moveTo: Int
) {
    fun oneByOne(state: List<Stack<Char>>) {
        val from = state[moveFrom - 1]
        val to = state[moveTo - 1]

        repeat(moveCount) {
            val item = from.pop()
            to.push(item)
        }
    }

    fun asBatch(state: List<Stack<Char>>) {
        val from = state[moveFrom - 1]
        val to = state[moveTo - 1]

        val tmp = Stack<Char>()
        repeat(moveCount) {
            tmp.push(from.pop())
        }

        repeat(moveCount) {
            to.push(tmp.pop())
        }
    }
}

fun main() {
    val asBatch = false
    val moves = Util.readInputForTaskAsLines()

    val state = initState()

    performMoves(moves, state, asBatch)
    printTopSymbols(state)
}

private fun initState(): List<Stack<Char>> {
/*

[Q] [J]                         [H]
[G] [S] [Q]     [Z]             [P]
[P] [F] [M]     [F]     [F]     [S]
[R] [R] [P] [F] [V]     [D]     [L]
[L] [W] [W] [D] [W] [S] [V]     [G]
[C] [H] [H] [T] [D] [L] [M] [B] [B]
[T] [Q] [B] [S] [L] [C] [B] [J] [N]
[F] [N] [F] [V] [Q] [Z] [Z] [T] [Q]
 1   2   3   4   5   6   7   8   9

*/


    val a = listOf(
        listOf('Q', 'G', 'P', 'R', 'L', 'C', 'T', 'F'),
        listOf('J', 'S', 'F', 'R', 'W', 'H', 'Q', 'N'),
        listOf('Q', 'M', 'P', 'W', 'H', 'B', 'F'),
        listOf('F', 'D', 'T', 'S', 'V'),
        listOf('Z', 'F', 'V', 'W', 'D', 'L', 'Q'),
        listOf('S', 'L', 'C', 'Z'),
        listOf('F', 'D', 'V', 'M', 'B', 'Z'),
        listOf('B', 'J', 'T'),
        listOf('H', 'P', 'S', 'L', 'G', 'B', 'N', 'Q')
    )

    return a.map {
        val res = Stack<Char>()
        res.addAll(it.reversed())

        res
    }
}

fun performMoves(moves: List<String>, state: List<Stack<Char>>, asBatch: Boolean) {
    moves.forEach {
        val move = parseMove(it)
        if (asBatch) {
            move.asBatch(state)
        } else {
            move.oneByOne(state)
        }
    }
}

private fun parseMove(line:String): MoveCommand {
    val regex = "move (\\d+) from (\\d+) to (\\d+)".toRegex() // Example: "move 1 from 8 to 1"

    val result = regex.find(line)!!

    val moveCount = result.groupValues[1].toInt()
    val moveFrom = result.groupValues[2].toInt()
    val moveTo = result.groupValues[3].toInt()

    return MoveCommand(moveCount, moveFrom, moveTo)
}

fun printTopSymbols(state: List<Stack<Char>>) {
    state.forEach { s ->
        print(s.pop())
    }

    println()
}



