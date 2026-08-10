plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Printscript2026"

include(":token")
include(":ast")
include(":lexer")
include(":parser")
include(":interpreter")