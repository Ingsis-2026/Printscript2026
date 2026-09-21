package interpreter

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

object InterpreterFactory {
    /** Los nodos que PrintScript 1.0 sabe ejecutar. */
    fun version10Evaluators(): List<NodeEvaluator> =
        listOf(
            LiteralEvaluator(),
            BinaryEvaluator(),
            AssignmentEvaluator(),
            DeclarationEvaluator(),
            PrintEvaluator(),
            BlockEvaluator(),
            NilEvaluator(),
        )

    /**
     * 1.1 es 1.0 más lo que la versión agrega al lenguaje: `if`/`else` y las funciones
     * `readInput` y `readEnv`.
     *
     * Se escribe como una suma para que la diferencia entre versiones esté dicha, en lugar de
     * quedar a la vista sólo de quien compare dos listas enteras.
     */
    fun version11Evaluators(): List<NodeEvaluator> = version10Evaluators() + ConditionalEvaluator() + FunctionEvaluator()

    fun evaluatorsFor(version: String): List<NodeEvaluator> =
        when (version) {
            "1.0" -> version10Evaluators()
            "1.1" -> version11Evaluators()
            else -> throw IllegalArgumentException("Unsupported version: $version")
        }

    fun forVersion(
        version: String,
        printer: Printer,
        reader: Reader,
    ): Interpreter = Interpreter(printer = printer, reader = reader, evaluators = evaluatorsFor(version))
}
