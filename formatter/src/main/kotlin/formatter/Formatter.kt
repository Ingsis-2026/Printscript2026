package formatter

import ast.ASTNode

interface Formatter {
    fun format(input: String): String

    fun format(astNode: ASTNode): String

    fun getRules(): Map<String, Any>
}
