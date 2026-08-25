package interpreter.evaluators

import ast.ASTNode
import ast.FunctionNode
import ast.LiteralNode
import interpreter.Interpreter
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
                        println(value)
                        value
                    }
                }
            }
            else -> throw RuntimeException("Unsupported function: ${functionNode.type}")
        }
    }

    private fun handleReadInput(
        node: FunctionNode,
        interpreter: Interpreter,
    ): Any? {
        val argument: LiteralNode =
            if (node.expression is LiteralNode) {
                node.expression as LiteralNode
            } else {
                throw RuntimeException("readInput necesita solo un argumento")
            }
        val message =
            interpreter.execute(argument) as? String
                ?: throw RuntimeException("El argumento de readInput debe ser String")

        interpreter.printer.print(argument.value)
        val userInput = interpreter.reader.input(message)
        return interpreter.convertInput(userInput)
    }

    private fun handleReadEnv(
        node: FunctionNode,
        interpreter: Interpreter,
    ): Any? {
        val argument: LiteralNode =
            if (node.expression is LiteralNode) {
                node.expression as LiteralNode
            } else {
                throw RuntimeException("readEnv necesita solo un argumento")
            }
        val varName =
            interpreter.execute(argument) as? String
                ?: throw RuntimeException("El argumento de readEnv debe ser String")

        return System.getenv(varName) ?: throw RuntimeException("La variable de entorno '$varName' no está definida")
    }
}
