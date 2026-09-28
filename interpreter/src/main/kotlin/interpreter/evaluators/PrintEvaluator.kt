package interpreter.evaluators

import ast.DataType
import ast.PrintNode
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class PrintEvaluator : NodeEvaluator<PrintNode> {
    override val nodeType = PrintNode::class

    override fun evaluate(
        node: PrintNode,
        interpreter: Interpreter,
    ): Any? {
        val value = interpreter.execute(node.expression) ?: throw InterpreterException("Invalid expression in PrintNode")

        // Un valor leído de afuera dentro de un println es un string, según la consigna.
        val resolved = if (value is ExternalInput) value.asType(DataType.STRING) else value

        interpreter.printer.print(resolved.toString())
        return null
    }
}
