import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatterTest11 {
    private val yamlPath = "src/test/resources/rules11.yaml"

    private val formatter =
        FormatterBuilderPS().build(
            yamlPath,
            "1.1",
        )

    @Test
    fun `test formatter with simple boolean declaration expression`() {
        val input = "const x:boolean=true"
        val formatted = formatter.format(input)
        assertEquals("const x : boolean = true;", formatted)
    }

    @Test
    fun `test formatter with conditional expression`() {
        val input = "if (x) { println(\"yes\") } else { println(\"no\") }"
        val formatted = formatter.format(input)
        assertEquals(
            """if (x) {
        |
        |
        |   println("yes");
        |} else {
        |
        |
        |   println("no");
        |}
            """.trimMargin(),
            formatted,
        )
    }
}
