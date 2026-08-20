package interpreter.evaluators

import ast.ASTNode
import ast.LiteralNode
import interpreter.Interpreter
import token.TokenType

class LiteralEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is LiteralNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val literal = node as LiteralNode
        return when (literal.type) {
            TokenType.NUMBERLITERAL -> {
                literal.value.toIntOrNull() ?: literal.value.toDoubleOrNull()
                    ?: throw RuntimeException("Invalid number literal: ${literal.value}")
            }
            TokenType.STRINGLITERAL -> literal.value
            TokenType.BOOLEANLITERAL ->
                when (literal.value) {
                    "true" -> true
                    "false" -> false
                    else -> throw RuntimeException("Invalid boolean value: ${literal.value}")
                }
            TokenType.DATA_TYPE -> literal.value
            TokenType.IDENTIFIER ->
                interpreter.variables[literal.value]
                    ?: throw RuntimeException("Undefined variable: ${literal.value}")
            else -> throw RuntimeException("Unsupported literal type: ${literal.type}")
        }
    }
}
