plugins {
    kotlin("jvm") version "1.7.22"
}

group = "com.adventofcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.7.22"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}