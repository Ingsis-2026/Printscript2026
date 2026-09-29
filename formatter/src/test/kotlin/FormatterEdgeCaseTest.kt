import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rules.BracePlacement
import rules.FormattingRules
import version.Version

/** Casos límite: entrada vacía, literales de texto, y versiones no soportadas. */
class FormatterEdgeCaseTest {
    private val builder = FormatterBuilderPS()

    @Test
    fun `formatting empty input returns empty string`() {
        assertEquals("", builder.build(FormattingRules(), Version.V1_0).format(""))
    }

    @Test
    fun `formatting blank input returns empty string`() {
        assertEquals("", builder.build(FormattingRules(), Version.V1_0).format("\n\n"))
    }

    @Test
    fun `string literals keep their quotes and their contents`() {
        val source = "let x: string = \"hola:  mundo = 5;\";"

        assertEquals(source, builder.build(FormattingRules(), Version.V1_0).format(source))
    }

    @Test
    fun `a call keeps its argument`() {
        val source = "let x:string=readInput(\"name\");"
        // Igual que en el TCK, la separación uniforme también separa los paréntesis de la llamada.
        val expected = "let x : string = readInput ( \"name\" );"

        assertEquals(expected, builder.build(FormattingRules(singleSpaceSeparation = true), Version.V1_1).format(source))
    }

    @Test
    fun `a semicolon inside a string does not end the statement`() {
        val source = "println(\";\");\nprintln(\"ok\");"

        assertEquals(source, builder.build(FormattingRules(lineBreakAfterStatement = true), Version.V1_0).format(source))
    }

    @Test
    fun `an opening brace inside a string is not moved by the brace rule`() {
        val source = "if (true)\n{\nprintln(\"{\");\n}"

        assertEquals(source, builder.build(FormattingRules(bracePlacement = BracePlacement.NEXT_LINE), Version.V1_1).format(source))
    }

    @Test
    fun `a closing brace inside a string does not close the block`() {
        val source = "if (true) {\nprintln(\"}\");\nprintln(\"x\");\n}"
        val expected = "if (true) {\n    println(\"}\");\n    println(\"x\");\n}"

        assertEquals(expected, builder.build(FormattingRules(indentInsideIf = 4), Version.V1_1).format(source))
    }

    @Test
    fun `an identifier containing if is not treated as a conditional`() {
        val source = "let ifCount:number=1;"

        assertEquals(source, builder.build(FormattingRules(), Version.V1_0).format(source))
    }

    @Test
    fun `an unsupported version is rejected`() {
        assertThrows<IllegalArgumentException> {
            builder.build("{}".byteInputStream(), "2.0")
        }
    }
}
