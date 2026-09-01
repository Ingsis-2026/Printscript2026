package formatoperations

import ast.ASTNode
import ast.FunctionNode
import formatter.Formatter

class FunctionFormatter : FormattingOperation {
    override fun canHandle(astNode: ASTNode): Boolean = astNode is FunctionNode

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a FunctionNode")
        val functionNode = node as FunctionNode
        return "${functionNode.functionName}(${formatter.format(functionNode.expression)})"
    }
}
