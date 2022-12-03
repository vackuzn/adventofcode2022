package com.adventofcode

object Util {
    fun readInputForTaskAsLines(): List<String> {
        return readInputForTask().split("\r\n")
    }

    private fun readInputForTask(): String {
        val task = getTask()
        return readResourceFileAsText("tasks/$task/input.txt")
    }

    private fun getTask(): String {
        val taskClassName = Thread.currentThread()
            .stackTrace
            .filterNot { it.className.contains("java.lang.Thread") }
            .filterNot { it.className == Util::class.qualifiedName }
            .map { it.className }
            .first()

        return taskClassName.split(".")[3]
    }

    private fun readResourceFileAsText(resourceFileName: String): String {
        val result = Util::class.java.classLoader.getResource(resourceFileName) ?: throw Exception("Resource file $resourceFileName not found")

        return result.readText()
    }
}
