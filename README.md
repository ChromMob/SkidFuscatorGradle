# SkidFuscatorGradle
## What is this?
This plugin integrates with the Gradle build system and automatically obfuscates the code in the jar file, includes all dependencies, so you do not need to manually search for them.

## How to use?
### settings.gradle

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven {
            url "https://jitpack.io"
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "me.chrommob.skidfuscatorgradle") {
                useModule("com.github.ChromMob.SkidFuscatorGradle:SkidFuscatorGradle:master-SNAPSHOT")
            }
        }
    }
}

rootProject.name = 'yourProject'
```

### build.gradle

```groovy
plugins {
    id 'java'
    id 'me.chrommob.skidfuscatorgradle'
}
```

### Optional: If you want to customize how deep we search dependenices configure it using maxDepth variable. The default value is 3 and you should only increase this if you get missing libraries warning.

```kotlin
tasks.withType<SkidFuscatorTask> {
    maxDepth = 3
}
```

### In the project dir create skidfuscator folder and put skidfuscator.jar into it.
1. Create folder skifuscator in your project.
2. Download latest jar from https://github.com/skidfuscatordev/skidfuscator-java-obfuscator/releases/.
3. Rename the downloaded file to skidfuscator.jar and move it to the folder we created.

![obrazek](https://user-images.githubusercontent.com/62996347/211897648-96f86a39-ed47-42ab-8495-2c36c69cf7b5.png)

4. If you want to use exclusion put config.txt into the skidfuscator folder.
   
   Official Documentation: https://skidfuscator.dev/docs/config

   config.txt example:
```HOCON
exempt: [
    "class{^me\\/chrommob\\/example\\/ExamplePlugin}",
    "class{^me\\/chrommob\\/example\\/loader\\/ExampleClass}",
]
```


### Compile your project like you would always do.
1. Run skidfuscate task.
2. In intellij you can use the gradle menu on the right.

![obrazek](https://user-images.githubusercontent.com/62996347/211897841-15481a45-5914-49ef-b2e1-803cf9302366.png)

3. If you do not use intellij run gradlew.bat skidfuscate on Windows or gradlew skidfuscate on MacOS and Linux.

### Obfuscation should complete and there should be new folder inside the skidfuscator folder with the obfuscated files.
1. The folder obfuscated jar is in project_location/skidfuscator/name_of_original_jar-/name_of_original_jar-out.jar
