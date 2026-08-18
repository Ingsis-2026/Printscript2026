plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":commons"))
    testImplementation(project(":lexer"))
}
