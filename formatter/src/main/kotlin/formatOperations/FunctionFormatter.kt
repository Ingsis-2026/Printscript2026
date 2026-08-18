package formatOperations

import ast.ASTNode
import ast.FunctionNode
import formatter.Formatter

class FunctionFormatter : FormattingOperation {
    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is FunctionNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a FunctionNode") else node as FunctionNode
        return formatter.format(node.expression)
    }
}
