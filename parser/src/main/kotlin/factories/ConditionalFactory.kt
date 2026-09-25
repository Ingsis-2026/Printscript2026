package factories

import ast.ASTNode
import ast.BlockNode
import ast.ConditionalNode
import ast.LiteralNode
import ast.NilNode
import parser.Parser
import parser.ParserException
import parser.closesBlock
import parser.closesParenthesis
import parser.continuesConditional
import parser.opensBlock
import parser.opensParenthesis
import parser.startsConditional
import parser.toLiteral
import token.Token

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

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.startsConditional }

    private fun readCondition(tokens: List<Token>): LiteralNode = tokens[openParenIndex(tokens) + 1].toLiteral()

    /** Índice del `)` que cierra la condición, a partir del cual se busca el bloque. */
    private fun readConditionEnd(tokens: List<Token>): Int {
        val open = openParenIndex(tokens)
        val close = tokens.indexOfFirst { it.closesParenthesis }
        if (close <= open) throw ParserException("Error parsing conditional: missing or misordered parentheses", tokens)
        return close
    }

    private fun openParenIndex(tokens: List<Token>): Int {
        val open = tokens.indexOfFirst { it.opensParenthesis }
        if (open < 0 || open + 1 >= tokens.size) {
            throw ParserException("Error parsing conditional: missing or misordered parentheses", tokens)
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
        if (!elseToken.continuesConditional) return NilNode

        val elseOpen = openingBraceFrom(tokens, thenClose + 1)
        if (tokens.subList(thenClose + 2, elseOpen).any { it.startsConditional }) {
            throw ParserException("Error parsing block: \"else if\" is not supported", tokens)
        }
        return blockOf(tokens, elseOpen, matchingBrace(tokens, elseOpen))
    }

    private fun openingBraceFrom(
        tokens: List<Token>,
        from: Int,
    ): Int {
        val offset = tokens.subList(from, tokens.size).indexOfFirst { it.opensBlock }
        if (offset < 0) throw ParserException("Error parsing block: missing or unbalanced braces", tokens)
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
            val token = tokens[index]
            if (token.opensBlock) depth++
            if (token.closesBlock) {
                depth--
                if (depth == 0) return index
            }
        }
        throw ParserException("Error parsing block: missing or unbalanced braces", tokens)
    }
}
