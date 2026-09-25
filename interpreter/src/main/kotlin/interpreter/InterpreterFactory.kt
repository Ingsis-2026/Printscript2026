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
import version.Version

object InterpreterFactory {
    /**
     * Los nodos que la versión sabe ejecutar. 1.1 es 1.0 más lo que la versión agrega al
     * lenguaje: `if`/`else` y las funciones `readInput` y `readEnv`.
     */
    fun evaluatorsFor(version: Version): List<NodeEvaluator> =
        when (version) {
            Version.V1_0 ->
                listOf(
                    LiteralEvaluator(),
                    BinaryEvaluator(),
                    AssignmentEvaluator(),
                    DeclarationEvaluator(),
                    PrintEvaluator(),
                    BlockEvaluator(),
                    NilEvaluator(),
                )
            Version.V1_1 -> evaluatorsFor(Version.V1_0) + ConditionalEvaluator() + FunctionEvaluator()
        }
}
