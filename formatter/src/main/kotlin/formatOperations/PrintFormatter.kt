package formatOperations

import ast.ASTNode
import ast.PrintNode
import formatter.Formatter

class PrintFormatter : FormattingOperation {
    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is PrintNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a PrintNode")
        val printNode = node as PrintNode
        val expression = formatter.format(printNode.expression)

        val lineBreak = formatter.getRules()["lineBreakPrintln"] as Int

        return "${"\n".repeat(lineBreak)}println($expression)"
    }
}
