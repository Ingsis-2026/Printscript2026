package interpreter.evaluators

import ast.DeclarationNode
import ast.NilNode
import interpreter.Declaration
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class DeclarationEvaluator : NodeEvaluator<DeclarationNode> {
    override val nodeType = DeclarationNode::class

    override fun evaluate(
        node: DeclarationNode,
        interpreter: Interpreter,
    ): Any? {
        if (interpreter.variables.isTaken(node.id)) {
            throw InterpreterException("La variable '${node.id}' ya ha sido declarada")
        }

        val value =
            if (node.expression is NilNode) {
                null
            } else {
                interpreter.execute(node.expression) ?: throw InterpreterException("Expresión inválida en la declaración")
            }

        // La declaración se registra aunque no traiga valor: una asignación posterior la necesita
        // para resolver un readInput y para rechazar la reasignación de un const.
        interpreter.variables.declare(
            node.id,
            Declaration(node.keyword, node.dataType),
        )

        // El tipo de un valor leído de afuera lo fija la variable que lo recibe.
        val resolved = if (value is ExternalInput) value.asType(node.dataType) else value

        if (resolved != null) {
            interpreter.variables.assign(node.id, resolved)
        }

        return resolved
    }
}
