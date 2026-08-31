package interpreter.evaluators

import ast.ASTNode
import ast.BlockNode
import interpreter.Interpreter

class BlockEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is BlockNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val block = node as BlockNode
        var result: Any? = null
        for (blockNode in block.nodes) {
            result = interpreter.execute(blockNode)
        }
        return result
    }
}
