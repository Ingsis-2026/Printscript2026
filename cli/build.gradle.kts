plugins {
    id("printscript.kotlin-application-conventions")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":linter"))
}

application {
    mainClass.set("cli.MainKt")
    applicationName = "printscript"
}
