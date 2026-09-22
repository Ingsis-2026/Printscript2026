package rules

import ast.ASTNode
import ast.BinaryNode
import ast.FunctionNode
import linter.BrokenRule

/**
 * Exige que [functionName] se llame con un argumento simple: una variable o un literal.
 *
 * La violación se informa en la sentencia y no en la llamada, porque la llamada suele estar
 * adentro de otra cosa —`let code: string = readInput(...)`— y lo que se señala es dónde
 * empieza la sentencia que la contiene.
 */
class CallArgumentRule(
    private val functionName: String,
    private val errorMessage: String,
) : Rule {
    override fun check(statement: ASTNode): List<BrokenRule> =
        statement
            .insideStatement()
            .mapNotNull { callOf(it) }
            .filter { it.name.equals(functionName, ignoreCase = true) }
            .filter { isExpression(it.argument) }
            .map { BrokenRule(errorMessage, statement.position) }
            .toList()

    /** Un operador o una llamada anidada son expresión; una variable o un literal, no. */
    private fun isExpression(argument: ASTNode): Boolean = argument is BinaryNode || argument is FunctionNode
}
