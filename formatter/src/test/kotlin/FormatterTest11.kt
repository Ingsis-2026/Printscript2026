import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rules.FormattingRules

/** Reglas que sólo existen en 1.1, donde hay bloques: la llave del "if" y su sangría. */
class FormatterTest11 {
    private fun format(
        source: String,
        rules: FormattingRules,
    ): String = FormatterBuilderPS().build(rules, "1.1").format(source)

    @Test
    fun `the brace goes up to the line of the if`() {
        val source =
            """
            |let something: boolean = true;
            |if (something)
            |{
            |  println("Entered if");
            |}
            """.trimMargin()

        val expected =
            """
            |let something: boolean = true;
            |if (something) {
            |  println("Entered if");
            |}
            """.trimMargin()

        assertEquals(expected, format(source, FormattingRules(braceOnSameLine = true)))
    }

    @Test
    fun `the brace goes down to its own line`() {
        val source =
            """
            |let something: boolean = true;
            |if (something) {
            |  println("Entered if");
            |}
            """.trimMargin()

        val expected =
            """
            |let something: boolean = true;
            |if (something)
            |{
            |  println("Entered if");
            |}
            """.trimMargin()

        assertEquals(expected, format(source, FormattingRules(braceOnSameLine = false)))
    }

    @Test
    fun `the indentation rule re-indents every nesting level`() {
        val source =
            """
            |let something: boolean = true;
            |if (something) {
            |  if (something) {
            |    println("Entered two ifs");
            |  }
            |}
            """.trimMargin()

        val expected =
            """
            |let something: boolean = true;
            |if (something) {
            |    if (something) {
            |        println("Entered two ifs");
            |    }
            |}
            """.trimMargin()

        assertEquals(expected, format(source, FormattingRules(indentInsideIf = 4)))
    }

    @Test
    fun `the indentation rule reaches the else block too`() {
        val source =
            """
            |if (a) {
            |print_placeholder;
            |} else {
            |print_placeholder;
            |}
            """.trimMargin().replace("print_placeholder", "println(\"x\")")

        val expected =
            """
            |if (a) {
            |  println("x");
            |} else {
            |  println("x");
            |}
            """.trimMargin()

        assertEquals(expected, format(source, FormattingRules(indentInsideIf = 2)))
    }

    @Test
    fun `without a brace rule the source keeps the placement it had`() {
        val source =
            """
            |const flag: boolean = false;
            |if (flag)
            |{
            |      println("kept");
            |}
            """.trimMargin()

        assertEquals(source, format(source, FormattingRules()))
    }
}
