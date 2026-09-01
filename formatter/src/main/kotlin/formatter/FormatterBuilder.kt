package formatter

interface FormatterBuilder {
    fun build(
        rulesPath: String,
        version: String,
    ): Formatter
}
