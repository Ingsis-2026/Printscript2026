package interpreter.evaluators

import ast.ASTNode
import ast.NilNode
import interpreter.Interpreter

class NilEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is NilNode

    override fun evaluate(node: ASTNode, interpreter: Interpreter): Any? = null
}
