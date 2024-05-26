plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.kilafath"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.test {
    useJUnitPlatform()
}
application {
    mainClass = "com.kilafath.AppKt"
}
kotlin {
    jvmToolchain(21)
}