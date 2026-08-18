plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    val testImplementation by configurations
    testImplementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
