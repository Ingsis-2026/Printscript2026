package formatOperations

import ast.ASTNode
import formatter.Formatter

interface FormattingOperation {
    fun canHandle(astNode: ASTNode): Boolean

    fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String
}
