package cli

import cli.commands.CommandFactory
import cli.progress.NoOpProgressReporter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Recorre la CLI de punta a punta: argumentos reales, archivos reales y el código de salida
 * que devuelve el proceso.
 */
class PrintScriptCliTest {
    private val output = CapturingOutput()
    private val resources = "src/test/resources"

    private fun cli(inputs: List<String> = emptyList()) =
        PrintScriptCli(
            output = output,
            commandFactory = CommandFactory(output, NoOpProgressReporter, ScriptedReader(inputs)),
        )

    @Test
    fun `validation de un fuente valido termina con exito`() {
        val exitCode = cli().run(listOf("validation", "$resources/example2.ps"))

        assertEquals(PrintScriptCli.EXIT_SUCCESS, exitCode)
        assertTrue(output.infoText().contains("Validación exitosa: 4 sentencias"))
    }

    @Test
    fun `validation informa el error de sintaxis con fila y columna`() {
        val exitCode = cli().run(listOf("validation", "$resources/testWithoutSemicolon.txt"))

        assertEquals(PrintScriptCli.EXIT_ERROR, exitCode)
        assertTrue(output.errorText().contains("línea 1, columna 1"))
        assertTrue(output.errorText().contains("hasta línea 1, columna 11"))
    }

    @Test
    fun `informa el error lexico con su ubicacion`() {
        val exitCode = cli().run(listOf("validation", "$resources/invalidCharacter.ps"))

        assertEquals(PrintScriptCli.EXIT_ERROR, exitCode)
        assertTrue(output.errorText().contains("Carácter inválido"))
        assertTrue(output.errorText().contains("línea 1, columna 20"))
    }

    @Test
    fun `execution imprime la salida esperada de la consigna`() {
        val exitCode = cli().run(listOf("execution", "$resources/example1.ps"))

        assertEquals(PrintScriptCli.EXIT_SUCCESS, exitCode)
        assertEquals("Joe Doe", output.resultText())
    }

    @Test
    fun `formatting escribe el resultado en el canal de resultado`() {
        val exitCode = cli().run(listOf("formatting", "$resources/test01.txt", "--config", "$resources/formatterRules.yaml"))

        assertEquals(PrintScriptCli.EXIT_SUCCESS, exitCode)
        assertEquals("let x : number = 8;", output.resultText())
    }

    @Test
    fun `formatting escribe en el archivo indicado`(
        @TempDir tempDir: File,
    ) {
        val destination = File(tempDir, "formateado.ps")

        val exitCode =
            cli().run(
                listOf(
                    "formatting",
                    "$resources/example1.ps",
                    "--config",
                    "$resources/formatterRules.yaml",
                    "--output",
                    destination.path,
                ),
            )

        assertEquals(PrintScriptCli.EXIT_SUCCESS, exitCode)
        assertEquals(
            "let name: string = \"Joe\";\nlet lastName: string = \"Doe\";\n\nprintln(name + \" \" + lastName);\n",
            destination.readText(),
        )
        assertTrue(output.results.isEmpty(), "el resultado fue al archivo, no a stdout")
    }

    @Test
    fun `analyzing falla cuando encuentra violaciones`() {
        val exitCode = cli().run(listOf("analyzing", "$resources/example1.ps", "--config", "$resources/linterRules.json"))

        assertEquals(PrintScriptCli.EXIT_ERROR, exitCode)
        assertTrue(output.errorText().contains("Println must not be called with an expression"))
        assertTrue(output.errorText().contains("línea 4"))
    }

    @Test
    fun `analyzing termina con exito cuando el fuente cumple`() {
        val exitCode = cli().run(listOf("analyzing", "$resources/test01.txt", "--config", "$resources/linterRules.json"))

        assertEquals(PrintScriptCli.EXIT_SUCCESS, exitCode)
    }

    @Test
    fun `usa la version 1 punto 1 cuando se la pide`() {
        val exitCode = cli().run(listOf("execution", "$resources/test20.txt", "--version", "1.1"))

        assertEquals(PrintScriptCli.EXIT_SUCCESS, exitCode)
        assertTrue(output.results.contains("if statement working correctly"))
    }

    @Test
    fun `rechaza en 1 punto 0 una palabra reservada de 1 punto 1, con su ubicacion`() {
        val exitCode = cli().run(listOf("validation", "$resources/constDeclaration.ps"))

        assertEquals(PrintScriptCli.EXIT_ERROR, exitCode)
        assertTrue(output.errorText().contains("Const declarations are not allowed in version 1.0"))
        assertTrue(output.errorText().contains("línea 1, columna 1"))
    }

    @Test
    fun `un error de argumentos muestra la linea de uso`() {
        val exitCode = cli().run(listOf("compilation", "$resources/example1.ps"))

        assertEquals(PrintScriptCli.EXIT_USAGE, exitCode)
        assertTrue(output.errorText().contains("Operación desconocida"))
        assertTrue(output.errorText().contains("Uso: printscript"))
    }

    @Test
    fun `un error de configuracion no muestra la linea de uso`() {
        val exitCode = cli().run(listOf("analyzing", "$resources/example1.ps", "--config", "$resources/linterRules11.json"))

        assertEquals(PrintScriptCli.EXIT_USAGE, exitCode)
        assertTrue(output.errorText().contains("Error de configuración"))
        assertTrue(output.errors.none { it.startsWith("Uso:") })
    }

    @Test
    fun `informa reglas de formateo mal formadas`() {
        val exitCode = cli().run(listOf("formatting", "$resources/test01.txt", "--config", "$resources/badFormatterRules.yaml"))

        assertEquals(PrintScriptCli.EXIT_USAGE, exitCode)
        assertTrue(output.errorText().contains("no es YAML ni JSON válido"))
    }

    @Test
    fun `informa un destino de salida inaccesible`() {
        val exitCode =
            cli().run(
                listOf(
                    "formatting",
                    "$resources/test01.txt",
                    "--config",
                    "$resources/formatterRules.yaml",
                    "--output",
                    "no/existe/salida.ps",
                ),
            )

        assertEquals(PrintScriptCli.EXIT_ERROR, exitCode)
        assertTrue(output.errorText().contains("Error de entrada/salida"))
    }

    @Test
    fun `funciona con las dependencias por defecto`() {
        val exitCode = PrintScriptCli(output).run(listOf("validation"))

        assertEquals(PrintScriptCli.EXIT_USAGE, exitCode)
        assertTrue(output.errorText().contains("Falta el archivo fuente"))
    }
}
