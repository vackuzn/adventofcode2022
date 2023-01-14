package com.adventofcode.tasks.t21

import com.adventofcode.Util

fun main() {
    part1() // 170_237_589_447_588
    part2() //   3_712_643_961_892
}

fun part1() {
    val rootMonkey = Parser().parse("root")
    println(rootMonkey.resolve())
}

fun part2() {
    val rootAst = Parser().parse("root")
    val variableNodeName = "humn"

    simplifyAst(rootAst, variableNodeName)
    val solver = LinearEquationSolver(rootAst as MathOperationNode, variableNodeName)
    val solution = solver.solveEquation()

    println(solution)
}

class LinearEquationSolver(
    private val rootAst: MathOperationNode,
    private val variableNodeName: String
) {
    fun solveEquation(): Long {
        val dx = 1_000_000_000_000_000

        val x1 = 0L
        val x2 = x1 + dx

        val y1 = getFunctionValue(x1)
        val y2 = getFunctionValue(x2)

        val k = (y2 - y1).toDouble() / (x2 - x1)
        val b = y1

        val solution = (-b/k).toLong()
        if (getFunctionValue(solution) == 0L) {
            return solution
        } else {
            throw Exception("Cannot find solution")
        }
    }

    private fun getFunctionValue(x: Long): Long {
        val visitor = SetVariableValueVisitor(variableNodeName, x)
        rootAst.updateAst(visitor)

        val left = rootAst.first.resolve()
        val right = rootAst.second.resolve()

        return left - right
    }
}

fun simplifyAst(rootMonkey: ASTNode, variableNodeName: String) {
    val visitor = SimplifyNodesNotDependentOnVisitor(variableNodeName)
    rootMonkey.updateAst(visitor)
}

class Parser {
    private val symbols: Map<String, String>

    init {
        val input = Util.readInputForTaskAsLines()

        symbols = input.associate { line ->
            val s = line.split(":").map { it.trim() }

            val name = s.first()
            val expr = s.last()

            name to expr
        }
    }

    fun parse(rootNodeName: String): ASTNode {
        val nodeExpr = symbols[rootNodeName]!!

        return parseExpr(rootNodeName, nodeExpr)
    }

    private fun parseExpr(name: String, expr: String): ASTNode {
        if (expr.toIntOrNull() != null) {
            return NumValue(name, expr.toLong())
        }

        return parseMathOperation(name, expr)
    }

    private fun parseMathOperation(name: String, expr: String): MathOperationNode {
        val r = """(\w+)\s([+\-*/])\s(\w+)""".toRegex()
        val res = r.find(expr)!!

        val firstNodeName = res.groupValues[1]
        val secondNodeName = res.groupValues[3]
        val op = when (res.groupValues[2]) {
            "+" -> OperationType.Plus
            "-" -> OperationType.Minus
            "*" -> OperationType.Multiply
            "/" -> OperationType.Divide

            else -> throw Exception("Unexpected type ${res.groupValues[2]}")
        }

        val firstNode = parseExpr(firstNodeName, symbols[firstNodeName]!!)
        val secondNode = parseExpr(secondNodeName, symbols[secondNodeName]!!)

        return MathOperationNode(name, firstNode, op, secondNode)
    }
}

interface IVisitor {
    fun visit(node: ASTNode): ASTNode
}

class SimplifyNodesNotDependentOnVisitor(private val nodeName: String) : IVisitor {
    override fun visit(node: ASTNode): ASTNode {
        if (node is MathOperationNode && !node.dependsOn(nodeName)) {
            return NumValue(node.name, node.resolve())
        }

        return node
    }
}

class SetVariableValueVisitor(private val nodeName: String, private val value: Long) : IVisitor {
    override fun visit(node: ASTNode): ASTNode {
        if (node.name == nodeName) {
            return NumValue(nodeName, value)
        }

        return node
    }
}

abstract class ASTNode(val name: String) {
    abstract fun resolve(): Long
    abstract fun dependsOn(name: String): Boolean
    abstract fun updateAst(visitor: IVisitor)
    abstract fun toStringExpr(): String
}

class NumValue(name: String, val v: Long) : ASTNode(name) {
    override fun resolve(): Long {
        return v
    }

    override fun dependsOn(name: String): Boolean {
        return false
    }

    override fun updateAst(visitor: IVisitor) {}

    override fun toStringExpr(): String {
        return v.toString()
    }
}

enum class OperationType(val char: Char) { Plus('+'), Minus('-'), Multiply('*'), Divide('/') }
class MathOperationNode(
    name: String,
    var first: ASTNode,
    private val op: OperationType,
    var second: ASTNode
) : ASTNode(name) {
    override fun resolve(): Long {
        return when (op) {
            OperationType.Plus -> first.resolve() + second.resolve()
            OperationType.Minus -> first.resolve() - second.resolve()
            OperationType.Multiply -> first.resolve() * second.resolve()
            OperationType.Divide -> first.resolve() / second.resolve()
        }
    }

    override fun dependsOn(name: String): Boolean {
        if (this.name == name) {
            return true
        }

        if (first.name == name || second.name == name) {
            return true
        }

        return first.dependsOn(name) || second.dependsOn(name)
    }

    override fun updateAst(visitor: IVisitor) {
        first = visitor.visit(first)
        second = visitor.visit(second)

        if (first is MathOperationNode) {
            first.updateAst(visitor)
        }
        if (second is MathOperationNode) {
            second.updateAst(visitor)
        }
    }

    override fun toStringExpr(): String {
        return "(${first.toStringExpr()} ${op.char} ${second.toStringExpr()})"
    }
}