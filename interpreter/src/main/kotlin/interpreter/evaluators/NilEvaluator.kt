package interpreter.evaluators

import ast.NilNode
import interpreter.Interpreter

class NilEvaluator : NodeEvaluator<NilNode> {
    override val nodeType = NilNode::class

    override fun evaluate(
        node: NilNode,
        interpreter: Interpreter,
    ): Any? = null
}
