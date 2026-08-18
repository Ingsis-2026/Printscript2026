package interpreter.evaluators

import ast.ASTNode
import ast.AssignationNode
import interpreter.Interpreter
import token.TokenType

class AssignmentEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is AssignationNode

    override fun evaluate(node: ASTNode, interpreter: Interpreter): Any? {
        val assignation = node as AssignationNode
        val value = interpreter.execute(assignation.expression) ?: throw RuntimeException("Invalid assignment in Assignment")

        println("Asignando a la variable '${assignation.id}' el valor $value")

        if (interpreter.variables.containsKey(assignation.id)) {
            if (interpreter.tiposDeVariables[assignation.id] == "const") {
                throw RuntimeException("No es posible reasignar una variable de tipo ${interpreter.tiposDeVariables[assignation.id]}")
            }
            val expectedType =
                when (interpreter.variables[assignation.id]) {
                    is Int -> TokenType.NUMBERLITERAL
                    is String -> TokenType.STRINGLITERAL
                    else -> throw RuntimeException("Unknown type for variable ${assignation.id}")
                }

            if ((expectedType == TokenType.NUMBERLITERAL && value !is Int) ||
                (expectedType == TokenType.STRINGLITERAL && value !is String)
            ) {
                throw RuntimeException("Invalid expression for type ${expectedType.name.lowercase()}")
            }
        }

        interpreter.variables[assignation.id] = value
        println("Valor asignado a '${assignation.id}' es ahora ${interpreter.variables[assignation.id]}")
        return value
    }
}
