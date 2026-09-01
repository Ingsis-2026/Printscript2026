import formatter.FormatterBuilderPS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/**
 * Casos límite del formatter: entrada vacía, expresiones que no son literales ni
 * operaciones binarias, e identificadores que contienen "if" como subcadena.
 */
class FormatterEdgeCaseTest {
    private val formatter10 = FormatterBuilderPS().build("src/test/resources/rules10.yaml", "1.0")
    private val formatter11 = FormatterBuilderPS().build("src/test/resources/rules11.yaml", "1.1")

    @Test
    fun `formatting empty input returns empty string`() {
        assertEquals("", formatter10.format(""))
    }

    @Test
    fun `formatting blank input returns empty string`() {
        assertEquals("", formatter10.format("\n\n"))
    }

    @Test
    fun `declaration initialised from readInput renders the call`() {
        val formatted = formatter11.format("let x:string=readInput(\"name\")")

        assertFalse(formatted.contains("null"), "La expresión no debe formatearse como \"null\": $formatted")
        assertEquals("let x : string = readInput(\"name\");", formatted)
    }

    @Test
    fun `identifier containing if as a substring still gets its semicolon`() {
        val formatted = formatter10.format("let ifCount:number=1")

        assertEquals("let ifCount : number = 1;", formatted)
    }

    @Test
    fun `input already ending in a semicolon is not double terminated`() {
        assertEquals("let x : number = 2;", formatter10.format("let x:number=2;"))
    }
}
