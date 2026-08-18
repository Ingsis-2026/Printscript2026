package interpreter.evaluators

import ast.ASTNode
import interpreter.Interpreter

interface NodeEvaluator {
    fun canEvaluate(node: ASTNode): Boolean
    fun evaluate(node: ASTNode, interpreter: Interpreter): Any?
}
