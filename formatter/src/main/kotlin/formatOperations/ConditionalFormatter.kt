package formatOperations

import ast.ASTNode
import ast.BlockNode
import ast.ConditionalNode
import ast.NilNode
import formatter.Formatter

class ConditionalFormatter : FormattingOperation {
    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is ConditionalNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a ConditionalNode")
        val conditionalNode = node as ConditionalNode
        val condition = formatter.format(conditionalNode.condition)

        val thenBlock = conditionalNode.thenBlock as BlockNode
        val elseBlock = if (conditionalNode.elseBlock != NilNode) conditionalNode.elseBlock as BlockNode else null

        return if (elseBlock == null) {
            "if ($condition) {\n${formatBlock(thenBlock.nodes, formatter)}\n}"
        } else {
            "if ($condition) {\n${formatBlock(thenBlock.nodes, formatter)}\n} else {\n${formatBlock(elseBlock.nodes, formatter)}\n}"
        }
    }

    private fun formatBlock(
        list: List<ASTNode>,
        formatter: Formatter,
    ): String {
        val indentationConditional = formatter.getRules()["conditionalIndentation"] as Int
        val formattedNodes = list.map { it -> formatter.format(it) }
        val result = formattedNodes.joinToString("\n")

        var resultWithIndentation = ""
        for (line in result.lines()) {
            resultWithIndentation +=
                if (line.isNotBlank()) {
                    " ".repeat(indentationConditional) + line + ";\n"
                } else {
                    line + "\n"
                }
        }
        return resultWithIndentation.trimEnd() // elimina salto de línea extra al final
    }
}
