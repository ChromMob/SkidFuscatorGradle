import org.gradle.kotlin.dsl.*

plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "me.chrommob.skidfuscatorgradle"
version = "1.0"
description = "A gradle plugin to obfuscate your code. Simply put skidfuscator.jar into into projectFolder/skidfuscator/"

repositories {
    mavenCentral()
}

pluginBundle {
    website = "https://skidfuscator.dev/"
    vcsUrl = "https://github.com/ChromMob/skidfuscatorgradle"
    description = "A gradle plugin to obfuscate your code. Simply put skidfuscator.jar into into projectFolder/skidfuscator/"
    tags = listOf("obfuscation", "skidfuscator")
}

gradlePlugin {

    plugins {
        register("skidfuscator") {
            displayName = "SkidfuscatorGradle"
            id = "me.chrommob.skidfuscatorgradle"
            implementationClass = "me.chrommob.skidfuscatorgradle.SkidFuscatorPlugin"
            description = "A gradle plugin to obfuscate your code. Simply put skidfuscator.jar into into projectFolder/skidfuscator/"
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
