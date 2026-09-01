package parser

import ast.DeclarationNode
import lexer.Lexer
import lexer.LexerException
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream

/**
 * La consigna exige que un fuente demasiado grande para la memoria pueda procesarse: "no se
 * puede primero hacer todo el análisis léxico para luego pasar al análisis semántico ya que
 * el primero consumiría toda la memoria disponible".
 *
 * Estos tests verifican que el flujo es realmente perezoso contando cuántas líneas del
 * fuente se llegan a leer. Si el pipeline materializara todo, el contador llegaría al total.
 */
class StreamingPipelineTest {
    private val lexer = Lexer(TokenMapper("1.0"))
    private val parser = Parser.forVersion("1.0")

    private companion object {
        const val HUGE_LINE_COUNT = 100_000
    }

    /** Fuente "enorme" que además cuenta las líneas efectivamente consumidas. */
    private class CountingSource(
        private val lineCount: Int,
    ) {
        var linesRead = 0
            private set

        fun lines(): Sequence<String> =
            (0 until lineCount)
                .asSequence()
                .map { index ->
                    linesRead++
                    "let x$index : number = $index;"
                }
    }

    @Test
    fun `parsing the first statement does not read the whole source`() {
        val source = CountingSource(HUGE_LINE_COUNT)

        val firstNode = parser.execute(lexer.convertToTokens(source.lines())).first()

        assertTrue(firstNode is DeclarationNode, "Se esperaba una declaración, hubo ${firstNode::class.simpleName}")
        assertEquals("x0", (firstNode as DeclarationNode).id)
        assertTrue(
            source.linesRead <= 2,
            "Sólo debían leerse las líneas necesarias para la primera sentencia, se leyeron ${source.linesRead}",
        )
    }

    @Test
    fun `taking a few statements reads only a few lines`() {
        val source = CountingSource(HUGE_LINE_COUNT)

        val nodes = parser.execute(lexer.convertToTokens(source.lines())).take(5).toList()

        assertEquals(5, nodes.size)
        assertEquals(listOf("x0", "x1", "x2", "x3", "x4"), nodes.map { (it as DeclarationNode).id })
        assertTrue(
            source.linesRead <= 6,
            "Se esperaban a lo sumo 6 líneas leídas, se leyeron ${source.linesRead}",
        )
    }

    @Test
    fun `the whole huge source can be traversed without holding it in memory`() {
        val source = CountingSource(HUGE_LINE_COUNT)

        // count() consume el flujo sin retener los nodos.
        val total = parser.execute(lexer.convertToTokens(source.lines())).count()

        assertEquals(HUGE_LINE_COUNT, total)
        assertEquals(HUGE_LINE_COUNT, source.linesRead)
    }

    @Test
    fun `an input stream can be parsed as a stream`() {
        val source = "let a : number = 1;\nlet b : number = 2;\nprintln(a + b);"
        val stream = ByteArrayInputStream(source.toByteArray())

        val nodes = parser.execute(lexer.convertToTokens(stream)).toList()

        assertEquals(3, nodes.size)
    }

    @Test
    fun `the list based api still works and delegates to the streaming one`() {
        val nodes = parser.execute(lexer.execute("let a : number = 1;\nlet b : number = 2;"))

        assertEquals(2, nodes.size)
        assertEquals(listOf("a", "b"), nodes.map { (it as DeclarationNode).id })
    }

    // --- Errores con posición (fila y columna de inicio y de fin) ---

    @Test
    fun `a lexer error reports the position of the offending lexeme`() {
        val exception =
            assertThrows<LexerException> {
                lexer.execute("let a : number = 1;\nlet b : number = #;")
            }

        val start = exception.startPosition
        val end = exception.endPosition
        assertEquals(1, start?.row, "El carácter inválido está en la segunda línea (fila 1)")
        assertEquals(17, start?.column)
        assertEquals(18, end?.column)
        assertTrue(exception.describe().contains("línea 2"), exception.describe())
    }

    @Test
    fun `an unterminated statement reports its span`() {
        val exception =
            assertThrows<ParserException> {
                parser.execute(lexer.execute("let a : number = 1"))
            }

        assertEquals("las sentencias deben finalizar con \";\", \"}\" o \"{\"", exception.message)
        assertEquals(0, exception.startPosition?.row)
        assertEquals(0, exception.startPosition?.column)
        assertEquals(18, exception.endPosition?.column)
    }

    @Test
    fun `a type inconsistency reports the span of the offending expression`() {
        val exception =
            assertThrows<ParserException> {
                parser.execute(lexer.execute("let a : number = \"hola\";"))
            }

        assertTrue(
            exception.message!!.contains("inconsistent"),
            exception.message,
        )
        assertEquals(0, exception.startPosition?.row)
        // El tramo señalado es la expresión, no la sentencia completa.
        assertTrue(
            exception.startPosition!!.column > 0,
            "Se esperaba la columna de la expresión, fue ${exception.startPosition}",
        )
    }
}
