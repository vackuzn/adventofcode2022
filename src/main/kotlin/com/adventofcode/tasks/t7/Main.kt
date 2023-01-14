package com.adventofcode.tasks.t7

import com.adventofcode.Util

fun main() {
    val tree = parseFsTree()

    println(calcPart1(tree)) // 2104783
    println(calcPart2(tree)) // 5883165
}

private fun parseFsTree(): Folder {
    val input = Util.readInputForTaskAsLines()
    val parser = InputParser()

    return parser.parse(input)
}

private fun calcPart1(tree: Folder): Int {
    val visitor = TotalFolderSizeVisitor( 100_000)
    tree.visit(visitor)

    return visitor.result
}

private fun calcPart2(tree: Folder): Int {
    val totalSpace = 70_000_000
    val requiredSpace = 30_000_000

    val spaceToFree = requiredSpace - (totalSpace - tree.getSize())

    val visitor = ClosestFolderSizeVisitor(spaceToFree)
    tree.visit(visitor)

    return visitor.closestCeilSize
}

class InputParser {
    private val rootFolder = Folder.createRootFolder()
    private var currentFolder = rootFolder
    private var currentCommand = ""

    fun parse(input: List<String>): Folder {
        for (line in input) {
            if (line.startsWith("$ cd")) {
                executeCdCommand(line)
            } else if (line == "$ ls") {
                currentCommand = "ls"
            } else {
                parseCommandOutput(line)
            }
        }

        return rootFolder
    }

    private fun executeCdCommand(line: String) {
        val cdTarget = line.substring(5)

        currentFolder = when (cdTarget) {
            ".." -> currentFolder.getParentFolder()
            "/" -> rootFolder
            else -> {
                currentFolder.getChildFolder(cdTarget)
            }
        }
    }

    private fun parseCommandOutput(line: String) {
        if (currentCommand == "ls") {
            parseLsOutput(line)
        } else {
            throw Exception("Unexpected command $currentCommand")
        }
    }

    private fun parseLsOutput(line: String) {
        val s = line.split(" ")
        if (s.first() == "dir") {
            val childDirName = s.last()
            currentFolder.appendChildFolder(childDirName)
        } else {
            val fileSize = s.first().toInt()
            val fileName = s.last()

            currentFolder.appendFile(fileName, fileSize)
        }
    }
}

class File(val name: String, val size: Int)

class Folder(val name: String, private val parentFolder: Folder?) {
    private val childFolders = mutableListOf<Folder>()
    private val childFiles = mutableListOf<File>()

    fun visit(visitor: IFolderVisitor) {
        visitor.visit(this)
        childFolders.forEach { it.visit(visitor) }
    }

    fun getSize(): Int {
        return childFiles.sumOf { it.size } + childFolders.sumOf { it.getSize() }
    }

    fun getParentFolder(): Folder {
        assert(!isRootFolder())

        return parentFolder!!
    }

    private fun isRootFolder(): Boolean {
        return parentFolder == null
    }

    fun getChildFolder(cdTarget: String): Folder {
        return childFolders.single { it.name == cdTarget }
    }

    fun appendChildFolder(childDirName: String) {
        assert(!childFolders.any { it.name == childDirName })

        childFolders.add(Folder(childDirName, this))
    }

    fun appendFile(fileName: String, fileSize: Int) {
        assert(!childFiles.any { it.name == fileName })

        childFiles.add(File(fileName, fileSize))
    }

    companion object {
        fun createRootFolder() = Folder(name = "", null)
    }
}

interface IFolderVisitor {
    fun visit(item: Folder)
}

class TotalFolderSizeVisitor(private val maxSize: Int): IFolderVisitor {
    var result = 0
    override fun visit(item: Folder) {
        if (item.getSize() <= maxSize) {
            result += item.getSize()
        }
    }
}

class ClosestFolderSizeVisitor(private val sizeToFree: Int): IFolderVisitor {
    var closestCeilSize = Int.MAX_VALUE
    override fun visit(item: Folder) {
        if (item.getSize() in sizeToFree until closestCeilSize) {
            closestCeilSize = item.getSize()
        }
    }
}