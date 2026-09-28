package interpreter.evaluators

import ast.LiteralNode
import interpreter.Interpreter
import interpreter.InterpreterException
import token.TokenType

class LiteralEvaluator : NodeEvaluator<LiteralNode> {
    override val nodeType = LiteralNode::class

    override fun evaluate(
        node: LiteralNode,
        interpreter: Interpreter,
    ): Any? =
        when (node.type) {
            TokenType.NUMBERLITERAL -> {
                node.value.toIntOrNull() ?: node.value.toDoubleOrNull()
                    ?: throw InterpreterException("Invalid number literal: ${node.value}")
            }
            TokenType.STRINGLITERAL -> node.value
            TokenType.BOOLEANLITERAL ->
                when (node.value) {
                    "true" -> true
                    "false" -> false
                    else -> throw InterpreterException("Invalid boolean value: ${node.value}")
                }
            TokenType.DATA_TYPE -> node.value
            TokenType.IDENTIFIER ->
                interpreter.variables.valueOf(node.value)
                    ?: throw InterpreterException("Undefined variable: ${node.value}")
            else -> throw InterpreterException("Unsupported literal type: ${node.type}")
        }
}
