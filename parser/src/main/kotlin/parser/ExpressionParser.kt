package parser

import ast.ASTNode
import ast.BinaryNode
import ast.FunctionNode
import ast.LiteralNode
import token.Token

/**
 * Convierte los tokens de una expresión en su nodo. Es el único lugar del parser que lo hace:
 * las declaraciones, las asignaciones, `println` y los argumentos de una llamada pasan por acá.
 */
internal object ExpressionParser {
    /** De la que liga menos a la que liga más: `a + b * c` se parte primero por el `+`. */
    private val PRECEDENCE_LEVELS = listOf(setOf("+", "-"), setOf("*", "/"))

    fun parse(tokens: List<Token>): ASTNode =
        when {
            isEnclosedInParentheses(tokens) -> parse(tokens.subList(1, tokens.lastIndex))
            tokens.size == 1 -> tokens.single().toLiteral()
            tokens.any { it.namesFunction } -> call(tokens)
            else -> operation(tokens)
        }

    private fun call(tokens: List<Token>): FunctionNode {
        val nameIndex = tokens.indexOfFirst { it.namesFunction }
        val name = tokens[nameIndex]
        val openingParenthesis = nameIndex + 1
        val closingParenthesis = tokens.lastIndex
        val argument = tokens.subList(openingParenthesis + 1, closingParenthesis)
        if (argument.isEmpty()) throw ParserException("${name.value} needs an argument", tokens)
        return FunctionNode(
            type = name.getType(),
            functionName = name.value,
            expression = parse(argument),
            position = name.getPosition(),
        )
    }

    private fun operation(tokens: List<Token>): ASTNode =
        PRECEDENCE_LEVELS.firstNotNullOfOrNull { operators -> splitAtFirst(tokens, operators) }
            ?: throw ParserException("Error in operation", tokens)

    /** Parte por el primer operador de [operators] que no esté dentro de un paréntesis. */
    private fun splitAtFirst(
        tokens: List<Token>,
        operators: Set<String>,
    ): BinaryNode? {
        var depth = 0
        for ((index, token) in tokens.withIndex()) {
            if (token.opensParenthesis) depth++
            if (token.closesParenthesis) depth--
            if (depth == 0 && operators.any { token.isOperator(it) }) {
                return BinaryNode(
                    left = parse(tokens.subList(0, index)),
                    right = parse(tokens.subList(index + 1, tokens.size)),
                    operator = token,
                    position = token.getPosition(),
                )
            }
        }
        return null
    }

    /** Si el `(` del principio es el que cierra el `)` del final, como en `(a + b)` y no en `(a) + (b)`. */
    private fun isEnclosedInParentheses(tokens: List<Token>): Boolean {
        if (tokens.size < 2 || !tokens.first().opensParenthesis || !tokens.last().closesParenthesis) return false
        var depth = 0
        for (token in tokens.subList(0, tokens.lastIndex)) {
            if (token.opensParenthesis) depth++
            if (token.closesParenthesis) depth--
            if (depth == 0) return false
        }
        return true
    }
}

internal fun Token.toLiteral(): LiteralNode = LiteralNode(value = value, type = getType(), position = getPosition())
