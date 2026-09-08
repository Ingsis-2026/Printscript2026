package cli

import cli.commands.FormattingCommand
import cli.io.FormattedSink
import cli.pipeline.ParsingPipeline
import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * La consigna advierte que un fuente puede no caber en memoria: la CLI tiene que ir
 * produciendo resultado a medida que lee, no después de leer todo.
 */
class StreamingTest {
    private val lineCount = 100_000

    /** Destino que sólo consume las primeras sentencias, para cortar el flujo a propósito. */
    private class TakingSink(
        private val limit: Int,
    ) : FormattedSink {
        val statements = mutableListOf<String>()

        override fun write(statements: Sequence<String>) {
            statements.take(limit).forEach { this.statements.add(it) }
        }
    }

    @Test
    fun `parsear las primeras sentencias no lee el fuente completo`() {
        val source = CountingSource(lineCount)

        val result = ParsingPipeline("1.0").consume(source) { nodes -> nodes.take(3).toList() }

        assertEquals(3, result.value.size)
        assertTrue(source.linesRead <= 4, "se leyeron ${source.linesRead} líneas de $lineCount")
    }

    @Test
    fun `formatear las primeras sentencias no lee el fuente completo`() {
        val source = CountingSource(lineCount)
        val sink = TakingSink(2)
        val formatter = FormatterBuilderPS().build("src/test/resources/formatterRules.yaml", "1.0")

        FormattingCommand(source, formatter, sink, CapturingOutput()).execute()

        assertEquals(listOf("let x0 : number = 0;", "let x1 : number = 1;"), sink.statements)
        assertTrue(source.linesRead <= 3, "se leyeron ${source.linesRead} líneas de $lineCount")
    }

    @Test
    fun `el avance se informa sentencia por sentencia mientras se parsea`() {
        val reporter = RecordingProgressReporter()
        val source = InMemorySource("p.ps", "let a : number = 1;\nlet b : number = 2;\nprintln(b);")

        ParsingPipeline("1.0", reporter).consume(source) { nodes -> nodes.count() }

        assertEquals(listOf(1, 2, 3), reporter.counts)
        assertEquals(3, reporter.finishedAt)
    }

    @Test
    fun `el avance acompana al parseo y no lo espera`() {
        val reporter = RecordingProgressReporter()
        val source = CountingSource(lineCount)

        ParsingPipeline("1.0", reporter).consume(source) { nodes -> nodes.take(2).toList() }

        assertEquals(listOf(1, 2), reporter.counts)
    }
}
