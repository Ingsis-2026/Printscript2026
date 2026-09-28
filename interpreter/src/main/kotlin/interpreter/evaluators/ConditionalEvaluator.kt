package interpreter.evaluators

import ast.ConditionalNode
import interpreter.Interpreter
import interpreter.InterpreterException

class ConditionalEvaluator : NodeEvaluator<ConditionalNode> {
    override val nodeType = ConditionalNode::class

    override fun evaluate(
        node: ConditionalNode,
        interpreter: Interpreter,
    ): Any? {
        val condition = interpreter.execute(node.condition)

        if (condition !is Boolean) {
            throw InterpreterException("Condition must evaluate to a boolean")
        }

        if (condition) {
            interpreter.execute(node.thenBlock)
        } else {
            interpreter.execute(node.elseBlock)
        }
        return null
    }
}
