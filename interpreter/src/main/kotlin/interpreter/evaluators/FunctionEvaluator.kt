package interpreter.evaluators

import ast.DataType
import ast.FunctionNode
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class FunctionEvaluator : NodeEvaluator<FunctionNode> {
    override val nodeType = FunctionNode::class

    override fun evaluate(
        node: FunctionNode,
        interpreter: Interpreter,
    ): Any? =
        when (node.functionName) {
            "readInput" -> readInput(node, interpreter)
            "readEnv" -> readEnv(node, interpreter)
            "println" -> printValue(node, interpreter)
            else -> throw InterpreterException("Unsupported function: ${node.functionName}")
        }

    /**
     * Devuelve el texto leído sin interpretar: el tipo lo fija el destino de la llamada.
     * Ver [ExternalInput].
     */
    private fun readInput(
        node: FunctionNode,
        interpreter: Interpreter,
    ): ExternalInput {
        val message = stringArgumentOf(node, interpreter, functionName = "readInput")

        // El argumento es el mensaje que se imprime antes de pedir el valor.
        interpreter.printer.print(message)
        return ExternalInput(interpreter.reader.input(message), "readInput")
    }

    private fun readEnv(
        node: FunctionNode,
        interpreter: Interpreter,
    ): ExternalInput {
        val varName = stringArgumentOf(node, interpreter, functionName = "readEnv")
        val value = System.getenv(varName) ?: undefinedEnvironmentVariable(varName)
        return ExternalInput(value, "readEnv")
    }

    /**
     * Imprime la expresión por el Printer inyectado, no por stdout.
     *
     * Un `println` escrito en el fuente no llega acá: `PrintlnFactory` se adelanta a
     * `FunctionFactory` en la cadena del parser y lo convierte en un `PrintNode`, que atiende
     * [PrintEvaluator]. Esta rama cubre un `FunctionNode` construido directamente.
     */
    private fun printValue(
        node: FunctionNode,
        interpreter: Interpreter,
    ): Any? {
        val value = interpreter.execute(node.expression)
        interpreter.printer.print(value.toString())
        return value
    }

    /**
     * El argumento puede ser cualquier expresión, como `readInput("Nombre" + "?")`, pero tiene que
     * dar un texto. Un valor leído de afuera que llega como argumento es, entonces, un texto.
     */
    private fun stringArgumentOf(
        node: FunctionNode,
        interpreter: Interpreter,
        functionName: String,
    ): String {
        val value = interpreter.execute(node.expression)
        val resolved = if (value is ExternalInput) value.asType(DataType.STRING) else value
        return resolved as? String
            ?: throw InterpreterException("El argumento de $functionName debe ser String")
    }

    private fun undefinedEnvironmentVariable(varName: String): Nothing =
        throw InterpreterException("La variable de entorno '$varName' no está definida")
}
