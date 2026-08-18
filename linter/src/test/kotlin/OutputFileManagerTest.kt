import linter.OutputFileManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OutputFileManagerTest {
    private val outputFileManager = OutputFileManager()

    @Test
    fun `test createTxtReport returns empty string for no broken rules`() {
        val report = outputFileManager.createTxtReport(emptyList())
        assertEquals("", report, "The report should be empty when no broken rules are provided")
    }

    @Test
    fun `test createHtmlReport returns basic structure for no broken rules`() {
        val report = outputFileManager.createHtmlReport(emptyList())
        val expectedReport = "<html><head><title>Broken Rules Report</title></head><body><h1>Broken Rules</h1><ul></ul></body></html>"
        assertEquals(expectedReport, report, "The HTML report should have the basic structure even with no broken rules")
    }
    /*
    @Test
    fun `test saveToFile writes content to specified file`() {
        val content = "Test content"
        val filePath = "test_output.txt"

        outputFileManager.saveToFile(content, filePath)

        // Verify the content of the file
        val writtenContent = File(filePath).readText()
        assertEquals(content, writtenContent, "The file content should match the expected content")

        // Clean up the test file
        File(filePath).delete()
    }
     */
}
