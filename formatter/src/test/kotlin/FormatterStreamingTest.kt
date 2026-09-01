import formatter.FormatterBuilderPS
import formatter.FormatterPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * El formatter debe poder recorrer un fuente que no cabe en memoria, emitiendo cada
 * sentencia formateada a medida que la resuelve.
 */
class FormatterStreamingTest {
    private val formatter =
        FormatterBuilderPS().build("src/test/resources/rules10.yaml", "1.0") as FormatterPS

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
    fun `formatting the first statements does not read the whole source`() {
        val source = CountingSource(100_000)

        val formatted = formatter.formatStatements(source.lines()).take(3).toList()

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

        val streamed = formatter.formatStatements(source.lineSequence()).toList().joinToString("\n")

        assertEquals(formatter.format(source), streamed)
    }
}
