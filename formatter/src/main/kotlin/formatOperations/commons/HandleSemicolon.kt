package formatOperations.commons

class HandleSemicolon {
    fun handleSemicolon(text: String): String {
        val result = text.trimEnd()
        return if (result != "" &&
            result.endsWith(";")
        ) {
            result
        } else {
            "$result;"
        }
    }
}
