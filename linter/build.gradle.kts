plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}
