package interpreter.evaluators

import ast.ASTNode
import ast.AssignationNode
import interpreter.Interpreter
import interpreter.InterpreterException
import token.TokenType

class AssignmentEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is AssignationNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val assignation = node as AssignationNode
        val value =
            interpreter.execute(assignation.expression)
                ?: throw InterpreterException("Invalid assignment in Assignment")

        if (interpreter.variables.containsKey(assignation.id)) {
            checkReassignable(assignation.id, value, interpreter)
        }

        interpreter.variables[assignation.id] = value
        return value
    }

    private fun checkReassignable(
        id: String,
        value: Any,
        interpreter: Interpreter,
    ) {
        val declarationKeyword = interpreter.tiposDeVariables[id]
        if (declarationKeyword == "const") {
            throw InterpreterException("No es posible reasignar una variable de tipo $declarationKeyword")
        }

        // Una variable sólo admite valores del mismo tipo con el que fue inicializada.
        val existing = interpreter.variables[id] ?: return
        if (existing::class != value::class) {
            throw InterpreterException("Invalid expression for type ${literalTypeNameOf(existing)}")
        }
    }

    /** Nombre del TokenType literal asociado al valor, sólo para los mensajes de error. */
    private fun literalTypeNameOf(value: Any): String =
        when (value) {
            is Int, is Double, is Float -> TokenType.NUMBERLITERAL
            is String -> TokenType.STRINGLITERAL
            is Boolean -> TokenType.BOOLEANLITERAL
            else -> TokenType.UNKNOWN
        }.name.lowercase()
}
