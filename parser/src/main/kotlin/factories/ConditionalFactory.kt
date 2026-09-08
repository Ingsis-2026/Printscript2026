package factories

import ast.ASTNode
import ast.BlockNode
import ast.ConditionalNode
import ast.LiteralNode
import ast.NilNode
import parser.Parser
import parser.ParserException
import token.Token
import token.TokenType

class ConditionalFactory(
    private val subParserProvider: () -> Parser = { Parser() },
) : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val closeParen = readConditionEnd(tokens)
        val conditionNode = readCondition(tokens)

        val thenOpen = openingBraceFrom(tokens, closeParen)
        val thenClose = matchingBrace(tokens, thenOpen)

        return ConditionalNode(
            condition = conditionNode,
            thenBlock = blockOf(tokens, thenOpen, thenClose),
            elseBlock = readElseBlock(tokens, thenClose),
            position = conditionNode.position,
        )
    }

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.getType() == TokenType.CONDITIONAL && it.value == "if" }

    private fun readCondition(tokens: List<Token>): LiteralNode {
        val conditionToken = tokens[openParenIndex(tokens) + 1]
        return LiteralNode(
            value = conditionToken.value,
            type = conditionToken.getType(),
            position = conditionToken.getPosition(),
        )
    }

    /** Índice del `)` que cierra la condición, a partir del cual se busca el bloque. */
    private fun readConditionEnd(tokens: List<Token>): Int {
        val open = openParenIndex(tokens)
        val close = tokens.indexOfFirst { it.value == ")" }
        if (close <= open) throw error(tokens, "Error parsing conditional: missing or misordered parentheses")
        return close
    }

    private fun openParenIndex(tokens: List<Token>): Int {
        val open = tokens.indexOfFirst { it.value == "(" }
        if (open < 0 || open + 1 >= tokens.size) {
            throw error(tokens, "Error parsing conditional: missing or misordered parentheses")
        }
        return open
    }

    /**
     * Bloque que arranca en [openIndex] y termina en su `}`.
     *
     * El cuerpo se pasa al sub-parser con el `}` de cierre incluido: para el splitter ese
     * token es el terminador de la última sentencia del bloque. Ver [Parser.executeBlockBody].
     */
    private fun blockOf(
        tokens: List<Token>,
        openIndex: Int,
        closeIndex: Int,
    ): ASTNode {
        if (closeIndex == openIndex + 1) return BlockNode(emptyList(), tokens[openIndex].getPosition())
        return BlockNode(
            nodes = subParserProvider().executeBlockBody(tokens.subList(openIndex + 1, closeIndex + 1)),
            position = tokens[openIndex + 1].getPosition(),
        )
    }

    /**
     * El `else` de este `if` es el que sigue inmediatamente al bloque del `then`.
     *
     * Buscarlo en toda la sentencia —como se hacía antes— tomaba el `else` de un `if`
     * anidado dentro del propio bloque.
     */
    private fun readElseBlock(
        tokens: List<Token>,
        thenClose: Int,
    ): ASTNode {
        val elseToken = tokens.getOrNull(thenClose + 1) ?: return NilNode
        if (elseToken.value != "else") return NilNode

        val elseOpen = openingBraceFrom(tokens, thenClose + 1)
        if (tokens.subList(thenClose + 2, elseOpen).any { it.value == "if" }) {
            throw error(tokens, "Error parsing block: \"else if\" is not supported")
        }
        return blockOf(tokens, elseOpen, matchingBrace(tokens, elseOpen))
    }

    private fun openingBraceFrom(
        tokens: List<Token>,
        from: Int,
    ): Int {
        val offset = tokens.subList(from, tokens.size).indexOfFirst { it.value == "{" }
        if (offset < 0) throw error(tokens, "Error parsing block: missing or unbalanced braces")
        return from + offset
    }

    /**
     * Índice del `}` que cierra la llave abierta en [openIndex].
     *
     * Cuenta la profundidad en lugar de tomar el primer `}`, que con bloques anidados cerraba
     * el bloque interno y dejaba el resto del cuerpo fuera del AST.
     */
    private fun matchingBrace(
        tokens: List<Token>,
        openIndex: Int,
    ): Int {
        var depth = 0
        for (index in openIndex until tokens.size) {
            when (tokens[index].value) {
                "{" -> depth++
                "}" -> {
                    depth--
                    if (depth == 0) return index
                }
            }
        }
        throw error(tokens, "Error parsing block: missing or unbalanced braces")
    }

    private fun error(
        tokens: List<Token>,
        message: String,
    ) = ParserException(
        message,
        tokens.firstOrNull()?.getPosition(),
        tokens.lastOrNull()?.getFinalPosition(),
    )
}
