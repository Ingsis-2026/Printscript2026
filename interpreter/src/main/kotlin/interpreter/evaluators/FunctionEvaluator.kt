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
        val functionNode = node as FunctionNode
        return when (functionNode.type) {
            TokenType.FUNCTION -> {
                when (functionNode.functionName) {
                    "readInput" -> handleReadInput(functionNode, interpreter)
                    "readEnv" -> handleReadEnv(functionNode, interpreter)
                    else -> {
                        val value = interpreter.execute(functionNode.expression)
                        // La salida va por el Printer inyectado, no por stdout.
                        interpreter.printer.print(value.toString())
                        value
                    }
                }
            }
            else -> throw InterpreterException("Unsupported function: ${functionNode.type}")
        }
    }

    /**
     * Devuelve el texto leído sin interpretar: el tipo lo fija el destino de la llamada.
     * Ver [ExternalInput].
     */
    private fun handleReadInput(
        node: FunctionNode,
        interpreter: Interpreter,
    ): ExternalInput {
        val message = stringArgumentOf(node, interpreter, "readInput")

        // El argumento es el mensaje que se imprime antes de pedir el valor.
        interpreter.printer.print(message)
        return ExternalInput(interpreter.reader.input(message), "readInput")
    }

    private fun handleReadEnv(
        node: FunctionNode,
        interpreter: Interpreter,
    ): ExternalInput {
        val varName = stringArgumentOf(node, interpreter, "readEnv")
        val value = System.getenv(varName) ?: undefinedEnvironmentVariable(varName)
        return ExternalInput(value, "readEnv")
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
