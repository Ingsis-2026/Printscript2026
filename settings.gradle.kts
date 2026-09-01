plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Printscript2026"

include(":lexer")
include(":parser")
include(":interpreter")
include(":commons")
include(":formatter")
include(":linter")
include(":cli")
