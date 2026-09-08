package interpreter.evaluators

import ast.ASTNode
import ast.PrintNode
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class PrintEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is PrintNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val printNode = node as PrintNode
        val value = interpreter.execute(printNode.expression) ?: throw InterpreterException("Invalid expression in PrintNode")

        // Un valor leído de afuera dentro de un println es un string, según la consigna.
        val resolved = if (value is ExternalInput) value.asType("string") else value

        interpreter.printer.print(resolved.toString())
        return Unit
    }
}
