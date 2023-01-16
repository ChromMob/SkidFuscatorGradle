import org.gradle.kotlin.dsl.*

plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "me.chrommob.skidfuscatorgradle"
version = "1.0.2"
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

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

