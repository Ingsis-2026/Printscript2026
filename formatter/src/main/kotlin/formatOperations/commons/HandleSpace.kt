package formatOperations.commons

class HandleSpace {
    fun handleSpace(
        tokenValue: String,
        spaceBefore: Boolean,
        spaceAfter: Boolean,
    ): String {
        var result = tokenValue.trim()
        if (spaceBefore) {
            result = " " + result
        }
        if (spaceAfter) {
            result += " "
        }
        return result
    }
}
