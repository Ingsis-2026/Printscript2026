package cli

import cli.arguments.ArgumentParser
import cli.arguments.CliUsageException
import cli.arguments.Operation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArgumentParserTest {
    private val parser = ArgumentParser()
    private val source = "src/test/resources/example1.ps"
    private val formatterRules = "src/test/resources/formatterRules.yaml"

    @Test
    fun `parsea la operacion y el archivo fuente`() {
        val arguments = parser.parse(listOf("validation", source))

        assertEquals(Operation.VALIDATION, arguments.operation)
        assertEquals(source, arguments.sourcePath)
        assertNull(arguments.configPath)
        assertNull(arguments.outputPath)
    }

    @Test
    fun `la version es opcional y por defecto es 1 punto 0`() {
        assertEquals("1.0", parser.parse(listOf("execution", source)).version)
    }

    @Test
    fun `toma la version indicada`() {
        assertEquals("1.1", parser.parse(listOf("execution", source, "--version", "1.1")).version)
    }

    @Test
    fun `toma la configuracion y la salida del formatting`() {
        val arguments = parser.parse(listOf("formatting", source, "--config", formatterRules, "--output", "salida.ps"))

        assertEquals(formatterRules, arguments.configPath)
        assertEquals("salida.ps", arguments.outputPath)
    }

    @Test
    fun `las opciones pueden venir en cualquier orden`() {
        val arguments = parser.parse(listOf("--version", "1.1", "formatting", "--config", formatterRules, source))

        assertEquals(Operation.FORMATTING, arguments.operation)
        assertEquals(source, arguments.sourcePath)
        assertEquals("1.1", arguments.version)
    }

    @Test
    fun `rechaza la falta de operacion`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(emptyList()) }
        assertTrue(exception.message.orEmpty().contains("Falta la operación"))
    }

    @Test
    fun `rechaza una operacion desconocida`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("compilation", source)) }
        assertTrue(exception.message.orEmpty().contains("Operación desconocida"))
    }

    @Test
    fun `rechaza la falta del archivo fuente`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("validation")) }
        assertTrue(exception.message.orEmpty().contains("Falta el archivo fuente"))
    }

    @Test
    fun `rechaza un archivo fuente inexistente`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("validation", "no-existe.ps")) }
        assertTrue(exception.message.orEmpty().contains("no existe"))
    }

    @Test
    fun `rechaza un directorio como archivo fuente`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("validation", "src/test/resources")) }
        assertTrue(exception.message.orEmpty().contains("no es un archivo"))
    }

    @Test
    fun `rechaza una version no soportada`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("validation", source, "--version", "9.9")) }
        assertTrue(exception.message.orEmpty().contains("Versión no soportada"))
    }

    @Test
    fun `exige configuracion para el formatting`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("formatting", source)) }
        assertTrue(exception.message.orEmpty().contains("archivo de configuración"))
    }

    @Test
    fun `exige configuracion para el analyzing`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("analyzing", source)) }
        assertTrue(exception.message.orEmpty().contains("archivo de configuración"))
    }

    @Test
    fun `rechaza una configuracion inexistente`() {
        val exception =
            assertFailsWith<CliUsageException> {
                parser.parse(listOf("formatting", source, "--config", "no-existe.yaml"))
            }
        assertTrue(exception.message.orEmpty().contains("configuración no existe"))
    }

    @Test
    fun `rechaza una opcion desconocida`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("validation", source, "--verbose", "si")) }
        assertTrue(exception.message.orEmpty().contains("Opción desconocida"))
    }

    @Test
    fun `rechaza una opcion sin valor`() {
        val exception = assertFailsWith<CliUsageException> { parser.parse(listOf("validation", source, "--version")) }
        assertTrue(exception.message.orEmpty().contains("necesita un valor"))
    }

    @Test
    fun `ignora la configuracion en las operaciones que no se configuran`() {
        val arguments = parser.parse(listOf("validation", source, "--config", "no-existe.yaml"))

        assertEquals("no-existe.yaml", arguments.configPath)
    }

    @Test
    fun `la linea de uso nombra las operaciones y las opciones`() {
        val usage = ArgumentParser.usage()

        assertTrue(usage.contains("validation|execution|formatting|analyzing"))
        assertTrue(usage.contains("--config"))
        assertTrue(usage.contains("--output"))
    }
}
