package cli.pipeline

import ast.ASTNode
import cli.io.Source
import cli.progress.NoOpProgressReporter
import cli.progress.ProgressReporter
import lexer.Lexer
import lexer.TokenMapper
import parser.Parser

/** Resultado de recorrer el fuente: lo que produjo el consumidor y cuántas sentencias vio. */
data class PipelineResult<T>(
    val value: T,
    val statements: Int,
)

/**
 * Encadena Lexer y Parser sobre un fuente, informando el avance mientras ocurre.
 *
 * Es el tramo que comparten Validation, Execution y Analyzing. El Formatter no la usa porque
 * `FormatterPS` hace su propio lexeo y parseo.
 *
 * Todo el recorrido es perezoso: [consume] entrega un flujo de nodos y el fuente se lee a
 * medida que el consumidor pide la sentencia siguiente, nunca de una sola vez.
 */
class ParsingPipeline(
    private val version: String,
    private val progressReporter: ProgressReporter = NoOpProgressReporter,
) {
    fun <T> consume(
        source: Source,
        block: (Sequence<ASTNode>) -> T,
    ): PipelineResult<T> {
        var parsed = 0
        val value =
            source.useLines { lines ->
                val nodes =
                    nodesFrom(lines).onEach {
                        parsed += 1
                        progressReporter.onStatementParsed(parsed)
                    }
                block(nodes)
            }
        progressReporter.onFinished(parsed)
        return PipelineResult(value, parsed)
    }

    private fun nodesFrom(lines: Sequence<String>): Sequence<ASTNode> {
        val tokens = Lexer(TokenMapper(version)).convertToTokens(lines)
        return Parser.forVersion(version).execute(tokens)
    }
}
