package formatoperations.commons

class SpaceHandler {
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
