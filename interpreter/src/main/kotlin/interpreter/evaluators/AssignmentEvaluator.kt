package interpreter.evaluators

import ast.ASTNode
import ast.AssignationNode
import interpreter.ExternalInput
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

        val resolved = resolveExternalInput(value, assignation.id, interpreter)

        if (interpreter.variables.containsKey(assignation.id)) {
            checkReassignable(assignation.id, resolved, interpreter)
        }

        interpreter.variables[assignation.id] = resolved
        return resolved
    }

    /** El tipo de un valor leído de afuera lo fija el tipo declarado de la variable destino. */
    private fun resolveExternalInput(
        value: Any,
        id: String,
        interpreter: Interpreter,
    ): Any {
        if (value !is ExternalInput) return value
        val declaredType =
            interpreter.declaredTypes[id]
                ?: throw InterpreterException("No se puede asignar ${value.origin} a '$id': la variable no fue declarada")
        return value.asType(declaredType)
    }

    private fun checkReassignable(
        id: String,
        value: Any,
        interpreter: Interpreter,
    ) {
        val declarationKeyword = interpreter.declarationKeywords[id]
        if (declarationKeyword == "const") {
            throw InterpreterException("No es posible reasignar una variable de tipo $declarationKeyword")
        }

        // Una variable sólo admite valores de su mismo tipo de PrintScript, y "number"
        // abarca enteros y decimales: reasignar 5 con 2.5 es válido.
        val existing = interpreter.variables[id] ?: return
        if (printScriptTypeOf(existing) != printScriptTypeOf(value)) {
            throw InterpreterException("Invalid expression for type ${literalTypeNameOf(existing)}")
        }
    }

    /** Tipo de PrintScript del valor: number (enteros y decimales), string o boolean. */
    private fun printScriptTypeOf(value: Any): TokenType =
        when (value) {
            is Number -> TokenType.NUMBERLITERAL
            is String -> TokenType.STRINGLITERAL
            is Boolean -> TokenType.BOOLEANLITERAL
            else -> TokenType.UNKNOWN
        }

    /** Nombre del TokenType literal asociado al valor, sólo para los mensajes de error. */
    private fun literalTypeNameOf(value: Any): String = printScriptTypeOf(value).name.lowercase()
}
