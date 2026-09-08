import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rules.FormattingRules

/** Casos límite: entrada vacía, literales de texto, y versiones no soportadas. */
class FormatterEdgeCaseTest {
    private val builder = FormatterBuilderPS()

    @Test
    fun `formatting empty input returns empty string`() {
        assertEquals("", builder.build(FormattingRules(), "1.0").format(""))
    }

    @Test
    fun `formatting blank input returns empty string`() {
        assertEquals("", builder.build(FormattingRules(), "1.0").format("\n\n"))
    }

    @Test
    fun `string literals keep their quotes and their contents`() {
        val source = "let x: string = \"hola:  mundo = 5;\";"

        assertEquals(source, builder.build(FormattingRules(), "1.0").format(source))
    }

    @Test
    fun `a call keeps its argument`() {
        val source = "let x:string=readInput(\"name\");"
        // Igual que en el TCK, la separación uniforme también separa los paréntesis de la llamada.
        val expected = "let x : string = readInput ( \"name\" );"

        assertEquals(expected, builder.build(FormattingRules(singleSpaceSeparation = true), "1.1").format(source))
    }

    @Test
    fun `an identifier containing if is not treated as a conditional`() {
        val source = "let ifCount:number=1;"

        assertEquals(source, builder.build(FormattingRules(), "1.0").format(source))
    }

    @Test
    fun `an unsupported version is rejected`() {
        assertThrows<IllegalArgumentException> {
            builder.build(FormattingRules(), "2.0")
        }
    }
}
