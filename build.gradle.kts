plugins {
    base
    kotlin("jvm") apply false
}

val installGitHooks by tasks.registering(Copy::class) {
    description = "Instala automáticamente los git hooks en .git/hooks"
    group = "git hooks"
    from("$rootDir/scripts/pre-commit")
    into("$rootDir/.git/hooks")
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.named("build") {
    dependsOn(installGitHooks)
}