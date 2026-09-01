plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    api(project(":commons"))
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
}
