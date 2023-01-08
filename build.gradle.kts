import com.gradle.publish.PublishPlugin
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.JavaPluginConvention
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

gradlePlugin {
    plugins {
        register("skidfuscator") {
            id = "me.chrommob.skidfuscatorgradle"
            implementationClass = "me.chrommob.skidfuscatorgradle.SkidFuscatorPlugin"
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

