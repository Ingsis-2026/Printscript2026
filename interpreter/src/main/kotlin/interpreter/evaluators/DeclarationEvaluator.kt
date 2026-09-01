package interpreter.evaluators

import ast.ASTNode
import ast.DeclarationNode
import ast.NilNode
import interpreter.Interpreter
import interpreter.InterpreterException

class DeclarationEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is DeclarationNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val declaration = node as DeclarationNode
        if (interpreter.variables.containsKey(declaration.id)) {
            throw InterpreterException("La variable '${declaration.id}' ya ha sido declarada")
        }

        val value =
            if (declaration.expr is NilNode) {
                Unit
            } else {
                interpreter.execute(declaration.expr) ?: throw InterpreterException("Expresión inválida en la declaración")
            }

        if (value != Unit) {
            interpreter.variables[declaration.id] = value
            interpreter.tiposDeVariables[declaration.id] = declaration.declValue
        }

        return value
    }
}
