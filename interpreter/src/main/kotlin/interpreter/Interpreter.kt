package interpreter

import ast.ASTNode
import interpreter.evaluators.NodeEvaluator

class Interpreter(
    val printer: Printer,
    val reader: Reader,
    private val evaluators: List<NodeEvaluator> = InterpreterFactory.defaultEvaluators(),
) {
    val variables: MutableMap<String, Any?> = mutableMapOf()
    val tiposDeVariables: MutableMap<String, String> = mutableMapOf()

    fun execute(node: ASTNode): Any? {
        val evaluator =
            evaluators.find { it.canEvaluate(node) }
                ?: throw RuntimeException("Unsupported node type: ${node::class.simpleName}")
        return evaluator.evaluate(node, this)
    }

    fun convertInput(input: String): Any =
        when {
            input.equals("true", ignoreCase = true) -> true
            input.equals("false", ignoreCase = true) -> false
            input.toIntOrNull() != null -> input.toInt()
            input.toDoubleOrNull() != null -> input.toDouble()
            else -> input // Devuelve la entrada como cadena si no se convierte
        }

    companion object {
        fun forVersion(
            version: String,
            printer: Printer,
            reader: Reader,
        ): Interpreter = InterpreterFactory.forVersion(version, printer, reader)
    }
}
