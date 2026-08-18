plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    implementation(project(":commons"))
    testImplementation(project(":lexer"))
}
