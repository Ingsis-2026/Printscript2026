package interpreter.evaluators

import ast.ASTNode
import ast.DeclarationNode
import ast.NilNode
import interpreter.ExternalInput
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

        // El tipo y el keyword se recuerdan aunque la declaración no tenga valor: una
        // asignación posterior los necesita para resolver un readInput y para rechazar la
        // reasignación de un const.
        interpreter.declaredTypes[declaration.id] = declaration.dataTypeValue
        interpreter.declarationKeywords[declaration.id] = declaration.declValue

        // El tipo de un valor leído de afuera lo fija la variable que lo recibe.
        val resolved = if (value is ExternalInput) value.asType(declaration.dataTypeValue) else value

        if (resolved != Unit) {
            interpreter.variables[declaration.id] = resolved
        }

        return resolved
    }
}
