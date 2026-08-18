package formatOperations

import ast.ASTNode
import ast.BinaryNode
import formatOperations.commons.HandleSpace
import formatter.Formatter

class BinaryFormatter : FormattingOperation {
    private val handleSpace: HandleSpace = HandleSpace()

    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is BinaryNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a BinaryNode")
        val binaryNode = node as BinaryNode
        val left = formatter.format(binaryNode.left)
        val right = formatter.format(binaryNode.right)
        return "$left ${binaryNode.operator.value} $right"
    }
}
