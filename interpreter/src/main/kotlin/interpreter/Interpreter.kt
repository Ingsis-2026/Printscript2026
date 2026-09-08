package interpreter

import ast.ASTNode
import interpreter.evaluators.NodeEvaluator

class Interpreter(
    val printer: Printer,
    val reader: Reader,
    private val evaluators: List<NodeEvaluator> = InterpreterFactory.defaultEvaluators(),
) {
    val variables: MutableMap<String, Any?> = mutableMapOf()

    /** Keyword con el que se declaró cada variable (`let` o `const`). */
    val declarationKeywords: MutableMap<String, String> = mutableMapOf()

    /**
     * Tipo declarado de cada variable (`string`, `number` o `boolean`).
     *
     * Se guarda para poder resolver un [ExternalInput] asignado más tarde, cuando el nodo de
     * la declaración ya no está a mano.
     */
    val declaredTypes: MutableMap<String, String> = mutableMapOf()

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

    companion object {
        fun forVersion(
            version: String,
            printer: Printer,
            reader: Reader,
        ): Interpreter = InterpreterFactory.forVersion(version, printer, reader)
    }
}
