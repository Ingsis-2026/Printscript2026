plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    api(project(":commons"))
    implementation(project(":lexer"))
    implementation("org.yaml:snakeyaml:2.0")
}
