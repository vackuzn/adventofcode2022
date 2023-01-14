package com.adventofcode.tasks.t8

import com.adventofcode.Util
import java.lang.Integer.max

fun main() {
    val input = Util.readInputForTaskAsLines()
    val grid = readInputAsGrid(input)
    val treeGrid = TreeGrid(grid)

    println(treeGrid.getVisibleTreesCount()) // 1827
    println(treeGrid.getMaxViewScore()) // 335580
}

private fun readInputAsGrid(input: List<String>): Grid {
    val grid = input.map { line ->
        line.map { tree -> tree.digitToInt() }
    }

    return Grid(grid)
}

class Grid(private val elements: List<List<Int>>) {
    private val heightIdx = elements.size - 1
    private val widthIdx = elements.first().size - 1

    operator fun get(h: Int, w: Int): Int {
        return elements[h][w]
    }

    fun iterateAllElementIndexes(action: (h: Int, w: Int) -> Unit) {
        for (h in 0..heightIdx) {
            for (w in 0..widthIdx) {
                action(h, w)
            }
        }
    }

    fun moveUp(h: Int, w: Int): Sequence<Int> {
        return sequence {
            for (idx in h - 1 downTo 0) {
                yield(get(idx, w))
            }
        }
    }

    fun moveDown(h: Int, w: Int): Sequence<Int> {
        return sequence {
            for (idx in h + 1..heightIdx) {
                yield(get(idx, w))
            }
        }
    }

    fun moveLeft(h: Int, w: Int): Sequence<Int> {
        return sequence {
            for (idx in w - 1 downTo 0) {
                yield(get(h, idx))
            }
        }
    }

    fun moveRight(h: Int, w: Int): Sequence<Int> {
        return sequence {
            for (idx in w + 1..widthIdx) {
                yield(get(h, idx))
            }
        }
    }
}

class TreeGrid(private val grid: Grid) {
    // Part 1
    fun getVisibleTreesCount(): Int {
        var visibleTreesCount = 0

        grid.iterateAllElementIndexes { h, w ->
            if (isTreeVisible(h, w)) {
                visibleTreesCount++
            }
        }

        return visibleTreesCount
    }

    private fun isTreeVisible(height: Int, width: Int): Boolean {
        val treeHeight = grid[height, width]

        val visibleFromTop = grid.moveUp(height, width).all { it < treeHeight }
        val visibleFromBottom = grid.moveDown(height, width).all { it < treeHeight }
        val visibleFromLeft = grid.moveLeft(height, width).all { it < treeHeight }
        val visibleFromRight = grid.moveRight(height, width).all { it < treeHeight }

        return visibleFromTop || visibleFromBottom || visibleFromLeft || visibleFromRight
    }

    // Part 2
    fun getMaxViewScore(): Int {
        var maxViewScore = 0

        grid.iterateAllElementIndexes { h, w ->
            if (isTreeVisible(h, w)) {
                val treeViewScore = getViewScore(h, w)
                maxViewScore = max(maxViewScore, treeViewScore)
            }
        }

        return maxViewScore
    }

    private fun getViewScore(height: Int, width: Int): Int {
        val treeHeight = grid[height, width]

        val viewUp = grid.moveUp(height, width).getViewDistance(treeHeight)
        val viewDown = grid.moveDown(height, width).getViewDistance(treeHeight)
        val viewLeft = grid.moveLeft(height, width).getViewDistance(treeHeight)
        val viewRight = grid.moveRight(height, width).getViewDistance(treeHeight)

        return viewUp * viewDown * viewLeft * viewRight
    }

    private fun Sequence<Int>.getViewDistance(treeHeight: Int): Int {
        var viewDistance = 0

        for (cellTreeHeight in this) {
            viewDistance++

            if (cellTreeHeight >= treeHeight) {
                break
            }
        }

        return viewDistance
    }
}