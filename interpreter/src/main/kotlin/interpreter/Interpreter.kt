package interpreter

import ast.ASTNode
import interpreter.evaluators.NodeEvaluator
import version.Version

class Interpreter(
    val printer: Printer,
    val reader: Reader,
    private val evaluators: List<NodeEvaluator> = InterpreterFactory.evaluatorsFor(Version.V1_1),
) {
    /**
     * Las variables del programa. Es pública porque un [NodeEvaluator] de terceros recibe el
     * intérprete entero y necesita poder leerlas y escribirlas.
     */
    val variables: VariableTable = VariableTable()

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
            version: Version,
            printer: Printer,
            reader: Reader,
        ): Interpreter = Interpreter(printer, reader, InterpreterFactory.evaluatorsFor(version))

        /** Para quien recibe la versión como texto, como el TCK. */
        fun forVersion(
            version: String,
            printer: Printer,
            reader: Reader,
        ): Interpreter = forVersion(Version.parse(version), printer, reader)
    }
}
