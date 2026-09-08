import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rules.FormattingRules

/**
 * Cada regla se prueba sola, que es como las entrega la configuración del TCK: un archivo
 * habilita una regla y espera que todo lo demás llegue al resultado igual que en el fuente.
 */
class FormatterTest10 {
    private fun format(
        source: String,
        rules: FormattingRules,
    ): String = FormatterBuilderPS().build(rules, "1.0").format(source)

    @Test
    fun `without rules the source is returned untouched`() {
        val source =
            """
            |let something:string = "a really cool thing";
            |let another_thing :   string="another one";
            |println( something );
            """.trimMargin()

        assertEquals(source, format(source, FormattingRules()))
    }

    @Test
    fun `spacing before the colon leaves the rest of the declaration alone`() {
        val source =
            """
            |let something:string = "one";
            |let another_thing : string="two";
            """.trimMargin()

        val expected =
            """
            |let something :string = "one";
            |let another_thing : string="two";
            """.trimMargin()

        assertEquals(expected, format(source, FormattingRules(spaceBeforeColon = true)))
    }

    @Test
    fun `spacing after the colon leaves the space before it as it was`() {
        val source =
            """
            |let something:string = "one";
            |let twice_thing : string = "two";
            |let third_thing :string="three";
            """.trimMargin()

        val expected =
            """
            |let something: string = "one";
            |let twice_thing : string = "two";
            |let third_thing : string="three";
            """.trimMargin()

        assertEquals(expected, format(source, FormattingRules(spaceAfterColon = true)))
    }

    @Test
    fun `spacing around equals is enforced on both sides`() {
        val source = "let something: string= \"one\";\nlet other: string =\"two\";"
        val expected = "let something: string = \"one\";\nlet other: string = \"two\";"

        assertEquals(expected, format(source, FormattingRules(spaceAroundEquals = true)))
    }

    @Test
    fun `no spacing around equals removes it on both sides`() {
        val source = "let something: string = \"one\";\nlet other: string =\"two\";"
        val expected = "let something: string=\"one\";\nlet other: string=\"two\";"

        assertEquals(expected, format(source, FormattingRules(spaceAroundEquals = false)))
    }

    @Test
    fun `single space separation puts one space between tokens and none before the semicolon`() {
        val source = "let something:      string=\"a really cool thing\";\nprintln(something);"
        val expected = "let something : string = \"a really cool thing\";\nprintln ( something );"

        assertEquals(expected, format(source, FormattingRules(singleSpaceSeparation = true)))
    }

    @Test
    fun `space surrounding operations does not touch the declaration`() {
        val source = "let result: number = 5+4*3/2;"
        val expected = "let result: number = 5 + 4 * 3 / 2;"

        assertEquals(expected, format(source, FormattingRules(spaceSurroundingOperations = true)))
    }

    @Test
    fun `a line break after every statement keeps the spacing of each one`() {
        val source = "let a:string = \"one\";let b : string = \"two\";let c :string=\"three\";"
        val expected = "let a:string = \"one\";\nlet b : string = \"two\";\nlet c :string=\"three\";"

        assertEquals(expected, format(source, FormattingRules(lineBreakAfterStatement = true)))
    }

    @Test
    fun `line breaks after println collapse the blank lines the source had`() {
        val source = "println(a);\n\n\n\n\nprintln(b);"

        assertEquals("println(a);\nprintln(b);", format(source, FormattingRules(lineBreaksAfterPrintln = 0)))
    }

    @Test
    fun `line breaks after println add the blank lines the source lacked`() {
        val source = "println(a);\nprintln(b);"

        assertEquals("println(a);\n\nprintln(b);", format(source, FormattingRules(lineBreaksAfterPrintln = 1)))
        assertEquals("println(a);\n\n\nprintln(b);", format(source, FormattingRules(lineBreaksAfterPrintln = 2)))
    }

    @Test
    fun `line breaks after println only apply after a println`() {
        val source = "let a: number = 1;\nprintln(a);\nlet b: number = 2;"

        assertEquals(
            "let a: number = 1;\nprintln(a);\n\nlet b: number = 2;",
            format(source, FormattingRules(lineBreaksAfterPrintln = 1)),
        )
    }

    @Test
    fun `the last println does not leave trailing blank lines`() {
        assertEquals("println(a);", format("println(a);", FormattingRules(lineBreaksAfterPrintln = 2)))
    }
}
