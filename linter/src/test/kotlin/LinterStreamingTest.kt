import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.Parser

/**
 * El linter debe analizar el programa sin retener el AST completo: consume el flujo de nodos
 * sentencia por sentencia y sólo acumula el reporte de violaciones.
 */
class LinterStreamingTest {
    private val lexer = Lexer(TokenMapper("1.0"))
    private val parser = Parser.forVersion("1.0")

    private class CountingSource(
        private val lineCount: Int,
        private val identifier: (Int) -> String,
    ) {
        var linesRead = 0
            private set

        fun lines(): Sequence<String> =
            (0 until lineCount)
                .asSequence()
                .map {
                    linesRead++
                    "let ${identifier(it)}:number=$it;"
                }
    }

    private fun linter(): Linter {
        val linter = Linter(LinterVersion.VERSION_1_0)
        linter.readJson("""{"identifier_format": "camelCase"}""")
        return linter
    }

    @Test
    fun `a huge source can be analysed and every violation reported`() {
        val source = CountingSource(20_000) { "my_var$it" }

        val output = linter().check(parser.execute(lexer.convertToTokens(source.lines())))

        assertEquals(20_000, source.linesRead, "Debe recorrerse todo el fuente")
        assertEquals(20_000, output.getBrokenRules().size, "Cada identificador snake_case viola camelCase")
        assertTrue(!output.isCorrect)
    }

    @Test
    fun `a clean huge source reports no violations`() {
        val source = CountingSource(20_000) { "myVar$it" }

        val output = linter().check(parser.execute(lexer.convertToTokens(source.lines())))

        assertTrue(output.isCorrect)
        assertTrue(output.getBrokenRules().isEmpty())
    }

    @Test
    fun `streaming and eager checking agree`() {
        val source = "let my_var:number=1;\nlet otherVar:number=2;"

        val streamed = linter().check(parser.execute(lexer.convertToTokens(source.lineSequence())))
        val eager = linter().check(parser.execute(lexer.execute(source)))

        assertEquals(eager.getBrokenRules().size, streamed.getBrokenRules().size)
        assertEquals(
            eager.getBrokenRules().map { it.ruleDescription },
            streamed.getBrokenRules().map { it.ruleDescription },
        )
    }
}
