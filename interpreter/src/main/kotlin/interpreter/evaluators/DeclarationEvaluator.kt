package interpreter.evaluators

import ast.ASTNode
import ast.DeclarationNode
import ast.NilNode
import interpreter.Declaration
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class DeclarationEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is DeclarationNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val declarationNode = node as DeclarationNode
        if (interpreter.variables.isTaken(declarationNode.id)) {
            throw InterpreterException("La variable '${declarationNode.id}' ya ha sido declarada")
        }

        val value =
            if (declarationNode.expr is NilNode) {
                Unit
            } else {
                interpreter.execute(declarationNode.expr) ?: throw InterpreterException("Expresión inválida en la declaración")
            }

        // La declaración se registra aunque no traiga valor: una asignación posterior la necesita
        // para resolver un readInput y para rechazar la reasignación de un const.
        interpreter.variables.declare(
            declarationNode.id,
            Declaration(declarationNode.declValue, declarationNode.dataType),
        )

        // El tipo de un valor leído de afuera lo fija la variable que lo recibe.
        val resolved = if (value is ExternalInput) value.asType(declarationNode.dataType) else value

        if (resolved != Unit) {
            interpreter.variables.assign(declarationNode.id, resolved)
        }

        return resolved
    }
}
