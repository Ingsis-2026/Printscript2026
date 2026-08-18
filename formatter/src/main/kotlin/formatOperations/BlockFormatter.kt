package formatOperations

import ast.ASTNode
import ast.BlockNode
import formatter.Formatter

class BlockFormatter : FormattingOperation {
    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is BlockNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        val blockNode = if (!canHandle(node)) error("Node isn't a BlockNode") else node as BlockNode
        val lines: List<String> = blockNode.nodes.map { it -> "${formatter.format(it)};" }
        val str = "\n".repeat(1)
        return lines.joinToString(str)
    }
}
