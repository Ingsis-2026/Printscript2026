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
            isCall(tokens) -> call(tokens)
            else -> operation(tokens)
        }

    /** Un nombre de función seguido de su argumento entre paréntesis: en `readInput("a") + "b"` la llamada es sólo un operando. */
    private fun isCall(tokens: List<Token>): Boolean =
        tokens.firstOrNull()?.namesFunction == true && isEnclosedInParentheses(tokens.drop(1))

    private fun call(tokens: List<Token>): FunctionNode {
        val name = tokens.first()
        if (name.callsPrintln) throw ParserException("println is a statement, not a value", tokens)
        val argument = tokens.subList(2, tokens.lastIndex)
        if (argument.isEmpty()) throw ParserException("${name.value} needs an argument", tokens)
        return FunctionNode(
            functionName = name.value,
            expression = parse(argument),
            position = name.getPosition(),
        )
    }

    private fun operation(tokens: List<Token>): ASTNode =
        PRECEDENCE_LEVELS.firstNotNullOfOrNull { operators -> splitAtLast(tokens, operators) }
            ?: throw ParserException("Error in operation", tokens)

    /** Parte por el último operador de [operators] que no esté dentro de un paréntesis: `a - b - c` es `(a - b) - c`. */
    private fun splitAtLast(
        tokens: List<Token>,
        operators: Set<String>,
    ): BinaryNode? {
        val index = lastTopLevelOperator(tokens, operators) ?: return null
        return BinaryNode(
            left = parse(tokens.subList(0, index)),
            right = parse(tokens.subList(index + 1, tokens.size)),
            operator = tokens[index],
            position = tokens[index].getPosition(),
        )
    }

    private fun lastTopLevelOperator(
        tokens: List<Token>,
        operators: Set<String>,
    ): Int? {
        var depth = 0
        var last: Int? = null
        for ((index, token) in tokens.withIndex()) {
            if (token.opensParenthesis) depth++
            if (token.closesParenthesis) depth--
            if (depth == 0 && operators.any { token.isOperator(it) }) last = index
        }
        return last
    }

    /** Si el `)` del final es el que cierra el `(` del principio: en `(a + b)` sí, en `(a) + (b)` no. */
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
