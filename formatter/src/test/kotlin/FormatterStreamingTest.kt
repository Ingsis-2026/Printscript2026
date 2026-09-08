import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rules.FormattingRules

/**
 * El formatter debe poder recorrer un fuente que no cabe en memoria, emitiendo cada línea
 * formateada a medida que la resuelve.
 */
class FormatterStreamingTest {
    private val formatter = FormatterBuilderPS().build(FormattingRules(singleSpaceSeparation = true), "1.0")

    private class CountingSource(
        private val lineCount: Int,
    ) {
        var linesRead = 0
            private set

        fun lines(): Sequence<String> =
            (0 until lineCount)
                .asSequence()
                .map {
                    linesRead++
                    "let x$it:number=$it;"
                }
    }

    @Test
    fun `formatting the first lines does not read the whole source`() {
        val source = CountingSource(100_000)

        val formatted = formatter.formatLines(source.lines()).take(3).toList()

        assertEquals(
            listOf("let x0 : number = 0;", "let x1 : number = 1;", "let x2 : number = 2;"),
            formatted,
        )
        assertTrue(
            source.linesRead <= 4,
            "Se esperaban a lo sumo 4 líneas leídas, se leyeron ${source.linesRead}",
        )
    }

    @Test
    fun `streaming and eager formatting agree`() {
        val source = "let x:number=1;\nlet y:number=2;"

        val streamed = formatter.formatLines(source.lineSequence()).toList().joinToString("\n")

        assertEquals(formatter.format(source), streamed)
    }
}
