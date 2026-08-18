package linter

enum class LinterVersion(val version: String) {
    VERSION_1_0("1.0"),
    VERSION_1_1("1.1"),
    ;

    companion object {
        fun fromString(version: String): LinterVersion? {
            return entries.find { it.version == version }
        }
    }
}
