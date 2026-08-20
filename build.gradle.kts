plugins {
    kotlin("jvm") apply false
}

tasks.register<Exec>("installGitHooks") {
    description = "Configura Git para usar los hooks versionados del proyecto (.githooks)"
    group = "help"

    commandLine("git", "config", "core.hooksPath", ".githooks")

    doLast {
        println("✅ Git hooks instalados exitosamente desde .githooks/")
    }
}