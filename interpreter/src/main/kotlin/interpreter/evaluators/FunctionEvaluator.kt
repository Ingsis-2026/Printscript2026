package interpreter.evaluators

import ast.ASTNode
import ast.FunctionNode
import ast.LiteralNode
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException
import token.TokenType

class FunctionEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is FunctionNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val call = node as FunctionNode
        if (call.type != TokenType.FUNCTION) throw InterpreterException("Unsupported function: ${call.type}")

        return when (call.functionName) {
            "readInput" -> readInput(call, interpreter)
            "readEnv" -> readEnv(call, interpreter)
            "println" -> printValue(call, interpreter)
            else -> throw InterpreterException("Unsupported function: ${call.functionName}")
        }
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

    private fun stringArgumentOf(
        node: FunctionNode,
        interpreter: Interpreter,
        functionName: String,
    ): String {
        val argument: LiteralNode =
            node.expression as? LiteralNode
                ?: throw InterpreterException("$functionName necesita solo un argumento")
        return interpreter.execute(argument) as? String
            ?: throw InterpreterException("El argumento de $functionName debe ser String")
    }

    private fun undefinedEnvironmentVariable(varName: String): Nothing =
        throw InterpreterException("La variable de entorno '$varName' no está definida")
}
