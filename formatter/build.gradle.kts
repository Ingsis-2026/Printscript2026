plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation("org.yaml:snakeyaml:2.0")
}
