package com.adventofcode.tasks.t2

import com.adventofcode.Util

enum class Figure(val value: Int) {
    Rock(1),
    Paper(2),
    Scissors(3);
}

enum class TargetResult { Win, Draw, Lose }

data class Round(
    val opponentFigure: Figure,
    val myFigure: Figure
) {
    val score = myFigure.value + getResultBonus()

    private fun getResultBonus(): Int {
        if (myFigure == opponentFigure) {
            return 3
        }

        return when {
            myFigure == Figure.Rock && opponentFigure == Figure.Scissors -> 6
            myFigure == Figure.Paper && opponentFigure == Figure.Rock -> 6
            myFigure == Figure.Scissors && opponentFigure == Figure.Paper -> 6
            else -> 0
        }
    }
}

fun main() {
    val secondCharIsExpectedResult = true
    val input = Util.readInputForTaskAsLines()

    val result = input
        .map { line -> readRound(line, secondCharIsExpectedResult) }
        .sumOf { round -> round.score }

    println(result)
}

private fun readRound(line: String, secondCharIsExpectedResult: Boolean): Round {
    val figures = line.split(" ")

    val opponentFigure = when (figures.first()) {
        "A" -> Figure.Rock
        "B" -> Figure.Paper
        "C" -> Figure.Scissors
        else -> throw Exception("Unexpected opponent figure code ${figures.first()}")
    }

    val myFigure = if (secondCharIsExpectedResult) {
        val expectedResult = getExpectedResult(figures.last())
        getMyFigureForExpectedResult(opponentFigure, expectedResult)
    } else {
        getMyFigure(figures.last())
    }

    return Round(opponentFigure, myFigure)
}

fun getExpectedResult(winChar: String): TargetResult {
    return when (winChar) {
        "X" -> TargetResult.Lose
        "Y" -> TargetResult.Draw
        "Z" -> TargetResult.Win
        else -> throw Exception("Unexpected target result code $winChar")
    }
}

fun getMyFigureForExpectedResult(opponentFigure: Figure, expectedResult: TargetResult): Figure {
    if (expectedResult == TargetResult.Draw) {
        return opponentFigure
    }

    return when (opponentFigure) {
        Figure.Rock -> if (expectedResult == TargetResult.Win) Figure.Paper else Figure.Scissors
        Figure.Paper -> if (expectedResult == TargetResult.Win) Figure.Scissors else Figure.Rock
        Figure.Scissors -> if (expectedResult == TargetResult.Win) Figure.Rock else Figure.Paper
    }
}

fun getMyFigure(figureChar: String): Figure {
    return when (figureChar) {
        "X" -> Figure.Rock
        "Y" -> Figure.Paper
        "Z" -> Figure.Scissors
        else -> throw Exception("Unexpected my figure code $figureChar")
    }
}
