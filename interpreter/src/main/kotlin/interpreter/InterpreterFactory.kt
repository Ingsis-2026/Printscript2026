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
