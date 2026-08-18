package formatOperations.commons

class HandleLineBreak {
    fun handleLineBreak(
        lines: List<String>,
        numberOfLineBreaks: Int,
    ): String {
        val str = "\n".repeat(numberOfLineBreaks)
        return lines.joinToString(str)
    }
}
