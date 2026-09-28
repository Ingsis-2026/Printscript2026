package interpreter.evaluators

import ast.BlockNode
import interpreter.Interpreter

class BlockEvaluator : NodeEvaluator<BlockNode> {
    override val nodeType = BlockNode::class

    override fun evaluate(
        node: BlockNode,
        interpreter: Interpreter,
    ): Any? {
        var result: Any? = null
        for (blockNode in node.nodes) {
            result = interpreter.execute(blockNode)
        }
        return result
    }
}
