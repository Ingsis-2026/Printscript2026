package interpreter

import ast.ASTNode
import interpreter.evaluators.AssignmentEvaluator
import interpreter.evaluators.BinaryEvaluator
import interpreter.evaluators.BlockEvaluator
import interpreter.evaluators.ConditionalEvaluator
import interpreter.evaluators.DeclarationEvaluator
import interpreter.evaluators.FunctionEvaluator
import interpreter.evaluators.LiteralEvaluator
import interpreter.evaluators.NilEvaluator
import interpreter.evaluators.NodeEvaluator
import interpreter.evaluators.PrintEvaluator

class Interpreter(
    val printer: Printer,
    val reader: Reader,
    private val evaluators: List<NodeEvaluator> = defaultEvaluators(),
) {
    val variables: MutableMap<String, Any?> = mutableMapOf()
    val tiposDeVariables: MutableMap<String, String> = mutableMapOf()

    fun execute(node: ASTNode): Any? {
        val evaluator =
            evaluators.find { it.canEvaluate(node) }
                ?: throw RuntimeException("Unsupported node type: ${node::class.simpleName}")
        return evaluator.evaluate(node, this)
    }

    fun convertInput(input: String): Any? =
        when {
            input.equals("true", ignoreCase = true) -> true
            input.equals("false", ignoreCase = true) -> false
            input.toIntOrNull() != null -> input.toInt()
            input.toDoubleOrNull() != null -> input.toDouble()
            else -> input // Devuelve la entrada como cadena si no se convierte
        }

    companion object {
        fun defaultEvaluators(): List<NodeEvaluator> =
            listOf(
                LiteralEvaluator(),
                BinaryEvaluator(),
                AssignmentEvaluator(),
                DeclarationEvaluator(),
                PrintEvaluator(),
                BlockEvaluator(),
                ConditionalEvaluator(),
                FunctionEvaluator(),
                NilEvaluator(),
            )

        fun forVersion(
            version: String,
            printer: Printer,
            reader: Reader,
        ): Interpreter =
            when (version) {
                "1.0" ->
                    Interpreter(
                        printer = printer,
                        reader = reader,
                        evaluators =
                            listOf(
                                LiteralEvaluator(),
                                BinaryEvaluator(),
                                AssignmentEvaluator(),
                                DeclarationEvaluator(),
                                PrintEvaluator(),
                                BlockEvaluator(),
                                NilEvaluator(),
                            ),
                    )
                "1.1" ->
                    Interpreter(
                        printer = printer,
                        reader = reader,
                        evaluators = defaultEvaluators(),
                    )
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }
    }
}
