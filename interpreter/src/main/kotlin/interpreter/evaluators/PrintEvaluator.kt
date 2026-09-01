package interpreter.evaluators

import ast.ASTNode
import ast.PrintNode
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
        interpreter.printer.print(value.toString())
        return Unit
    }
}
