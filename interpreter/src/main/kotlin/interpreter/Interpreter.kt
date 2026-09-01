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

    /**
     * Evalúa un nodo y garantiza que todo error salga con su ubicación.
     *
     * Los evaluadores lanzan [InterpreterException] sin posición, porque el dato que tienen a
     * mano es el nodo. Al atraparla acá y completarla sólo cuando aún no tiene posición, el
     * error queda ubicado en el nodo *más interno* que falló: las llamadas recursivas la
     * completan primero y las externas la vuelven a lanzar sin tocarla.
     */
    fun execute(node: ASTNode): Any? {
        val evaluator =
            evaluators.find { it.canEvaluate(node) }
                ?: throw InterpreterException("Unsupported node type: ${node::class.simpleName}", node.position)
        return try {
            evaluator.evaluate(node, this)
        } catch (exception: InterpreterException) {
            if (exception.startPosition != null) throw exception
            throw InterpreterException(exception.message ?: "", node.position)
        }
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
