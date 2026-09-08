package formatoperations

import ast.ASTNode
import ast.BlockNode
import ast.ConditionalNode
import ast.NilNode
import formatter.Formatter

class ConditionalFormatter : FormattingOperation {
    override fun canHandle(astNode: ASTNode): Boolean = astNode is ConditionalNode

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a ConditionalNode")
        val conditionalNode = node as ConditionalNode
        val condition = formatter.format(conditionalNode.condition)

        val thenBlock = conditionalNode.thenBlock as BlockNode
        val elseBlock = if (conditionalNode.elseBlock != NilNode) conditionalNode.elseBlock as BlockNode else null

        val thenBody = formatBlock(thenBlock.nodes, formatter)

        return if (elseBlock == null) {
            "if ($condition) {\n$thenBody\n}"
        } else {
            "if ($condition) {\n$thenBody\n} else {\n${formatBlock(elseBlock.nodes, formatter)}\n}"
        }
    }

    private fun formatBlock(
        list: List<ASTNode>,
        formatter: Formatter,
    ): String {
        val indentation = formatter.getRules()["conditionalIndentation"] as Int
        return list.joinToString("\n") { node -> indent(terminate(formatter.format(node), node), indentation) }
    }

    /** Un `if` anidado ya cierra con `}`: el `;` sólo termina las sentencias simples. */
    private fun terminate(
        formatted: String,
        node: ASTNode,
    ): String = if (node is ConditionalNode) formatted else "$formatted;"

    /**
     * Corre cada línea [indentation] espacios.
     *
     * Se indenta el texto ya formateado, línea por línea, para que un bloque anidado —que
     * ocupa varias líneas— quede corrido entero y no sólo en su primera línea.
     */
    private fun indent(
        formatted: String,
        indentation: Int,
    ): String =
        formatted
            .lines()
            .joinToString("\n") { line -> if (line.isBlank()) line else " ".repeat(indentation) + line }
}
