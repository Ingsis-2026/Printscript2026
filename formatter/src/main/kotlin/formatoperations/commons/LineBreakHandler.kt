package formatoperations.commons

class LineBreakHandler {
    fun handleLineBreak(
        lines: List<String>,
        numberOfLineBreaks: Int,
    ): String {
        val str = "\n".repeat(numberOfLineBreaks)
        return lines.joinToString(str)
    }
}
