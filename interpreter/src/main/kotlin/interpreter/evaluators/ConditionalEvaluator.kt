package interpreter.evaluators

import ast.ASTNode
import ast.ConditionalNode
import interpreter.Interpreter

class ConditionalEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is ConditionalNode

    override fun evaluate(node: ASTNode, interpreter: Interpreter): Any? {
        val conditional = node as ConditionalNode
        val condition = interpreter.execute(conditional.condition)

        if (condition !is Boolean) {
            throw RuntimeException("Condition must evaluate to a boolean")
        }

        if (condition) {
            interpreter.execute(conditional.thenBlock)
        } else {
            conditional.elseBlock?.let {
                interpreter.execute(it)
            }
        }
        return Unit
    }
}
