package com.adventofcode.tasks.t10

import com.adventofcode.Util
import java.util.*


class CpuEmulator(
    private val commands: Stack<CpuCommand>,
    private val cycleCompleted: (cpu: CpuEmulator) -> Unit
) {
    var currentCycle = 0
    var x = 1

    fun runProgram() {
        var currentCommand = commands.pop()

        while (true) {
            currentCycle++

            currentCommand.nextCycle(this)

            if (currentCommand.isCompleted()) {
                if (commands.isEmpty()) {
                    return
                }
                currentCommand = commands.pop()

                currentCommand.nextCycle(this)
            }

            cycleCompleted(this)
        }
    }
}

abstract class CpuCommand(
    private val cyclesRequired: Int
) {
    enum class State { NotStarted, Running, Completed }

    private var state = State.NotStarted
    private var cycleWhenStarted = -1

    fun isCompleted(): Boolean {
        return state == State.Completed
    }

    fun nextCycle(cpu: CpuEmulator) {
        if (state == State.NotStarted) {
            cycleWhenStarted = cpu.currentCycle

            state = State.Running
        }

        if (cpu.currentCycle >= cycleWhenStarted + cyclesRequired ) {
            state = State.Completed
            onCommandCompleted(cpu)
        }
    }

    protected abstract fun onCommandCompleted(cpu: CpuEmulator)
}

class NoopCommand: CpuCommand(1) {
    override fun onCommandCompleted(cpu: CpuEmulator) {
        // No op
    }
}

class AddXCommand(private val valueToAdd: Int): CpuCommand(2) {
    override fun onCommandCompleted(cpu: CpuEmulator) {
        cpu.x += valueToAdd
    }
}


fun main() {
    val input = Util.readInputForTaskAsLines()
    part1(input)
    part2(input)
}

fun part1(input: List<String>) {
    val commands = parseCommands(input)

    var sumStrengths = 0

    val cpu = CpuEmulator(commands) {
        val logOnClockValues = listOf(20, 60, 100, 140, 180, 220)
        if (it.currentCycle !in logOnClockValues) {
            return@CpuEmulator
        }

        val strength = it.currentCycle * it.x
        sumStrengths += strength
    }

    cpu.runProgram()

    println(sumStrengths)
}

fun part2(input: List<String>) {
    val commands = parseCommands(input)

    val cpu = CpuEmulator(commands) {
        // new line
        if ((it.currentCycle - 1).mod(40) == 0) {
            println()
        }

        val spriteStartPos = it.x
        val spriteEndPos = it.x + 2

        val linePos = it.currentCycle.mod(40)

        if (linePos in spriteStartPos .. spriteEndPos) {
            print("H")
        } else {
            print(".")
        }
    }

    cpu.runProgram()

    println()
}

fun parseCommands(input: List<String>): Stack<CpuCommand> {
    val commands = input.map { line ->
        val s = line.split(" ")

        val command = when (val commandName = s.first()) {
            "addx" -> {
                val valueToAdd = s.last().toInt()
                AddXCommand(valueToAdd)
            }
            "noop" -> NoopCommand()
            else -> throw Exception("Unexpected command $commandName")
        }

        command
    }

    val s = Stack<CpuCommand>()
    s.addAll(commands.reversed())

    return s
}
