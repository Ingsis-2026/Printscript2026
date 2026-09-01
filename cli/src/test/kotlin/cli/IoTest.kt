package cli

import cli.io.ConsoleOutput
import cli.io.ConsoleReader
import cli.io.FileSink
import cli.io.FileSource
import cli.io.OutputPrinter
import cli.io.OutputSink
import cli.progress.ConsoleProgressReporter
import cli.progress.NoOpProgressReporter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IoTest {
    @Test
    fun `el resultado va a stdout y el resto a stderr`() {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val output = ConsoleOutput(PrintStream(out), PrintStream(err))

        output.result("resultado")
        output.info("aviso")
        output.error("problema")

        assertEquals("resultado", out.toString().trim())
        assertTrue(err.toString().contains("aviso"))
        assertTrue(err.toString().contains("problema"))
    }

    @Test
    fun `el fuente se lee por lineas y expone su nombre`(
        @TempDir tempDir: File,
    ) {
        val file = File(tempDir, "programa.ps")
        file.writeText("let x : number = 1;\nprintln(x);")
        val source = FileSource(file)

        assertEquals("programa.ps", source.name)
        assertEquals(listOf("let x : number = 1;", "println(x);"), source.useLines { it.toList() })
    }

    @Test
    fun `el fuente se puede construir desde una ruta`() {
        assertEquals("example1.ps", FileSource("src/test/resources/example1.ps").name)
    }

    @Test
    fun `el destino de archivo escribe una sentencia por linea`(
        @TempDir tempDir: File,
    ) {
        val file = File(tempDir, "salida.ps")

        FileSink(file).write(sequenceOf("let x : number = 1;", "println(x);"))

        assertEquals("let x : number = 1;\nprintln(x);\n", file.readText())
    }

    @Test
    fun `el destino de archivo se puede construir desde una ruta`(
        @TempDir tempDir: File,
    ) {
        val path = File(tempDir, "porRuta.ps").path

        FileSink(path).write(sequenceOf("println(1);"))

        assertEquals("println(1);\n", File(path).readText())
    }

    @Test
    fun `el destino por defecto escribe en el canal de resultado`() {
        val output = CapturingOutput()

        OutputSink(output).write(sequenceOf("uno", "dos"))

        assertEquals(listOf("uno", "dos"), output.results)
    }

    @Test
    fun `la salida del programa interpretado es el resultado de la CLI`() {
        val output = CapturingOutput()

        OutputPrinter(output).print("hola")

        assertEquals(listOf("hola"), output.results)
        assertTrue(output.info.isEmpty())
    }

    @Test
    fun `la entrada estandar entrega una linea por lectura`() {
        val reader = ConsoleReader("Ana\nJuan\n".reader().buffered())

        assertEquals("Ana", reader.input("nombre: "))
        assertEquals("Juan", reader.input("nombre: "))
        assertEquals("", reader.input("nombre: "))
    }

    @Test
    fun `el avance se muestra sobre la misma linea de stderr`() {
        val err = ByteArrayOutputStream()
        val reporter = ConsoleProgressReporter(PrintStream(err))

        reporter.onStatementParsed(1)
        reporter.onStatementParsed(2)
        reporter.onFinished(2)

        val text = err.toString()
        assertTrue(text.contains("\rAnalizando... 1 sentencias"))
        assertTrue(text.contains("\rAnalizando... 2 sentencias. Listo."))
    }

    @Test
    fun `el reportador nulo no informa nada`() {
        NoOpProgressReporter.onStatementParsed(1)
        NoOpProgressReporter.onFinished(1)
    }
}
