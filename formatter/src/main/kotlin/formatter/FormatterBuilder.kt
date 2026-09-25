package formatter

import version.Version

interface FormatterBuilder {
    fun build(
        rulesPath: String,
        version: Version,
    ): Formatter
}
