# SkidFuscatorGradle
## How to use?
### Clone this repo and publish it to local maven.
![obrazek](https://user-images.githubusercontent.com/62996347/211895035-c32db81d-98b8-4d6e-bad9-9010b0c0077e.png)

1. Extract the zip.

![obrazek](https://user-images.githubusercontent.com/62996347/211895609-336a364f-115b-41e8-9684-86e80c5d6f6e.png)

2. Open CMD or Terminal linux inside the SkidFuscatorGradle folder and execute gradlew.bat publishToMavenLocal on Windows systems. On MacOS and Linux run gradlew publishToMavenLocal.
  
3. SkidFuscatorGradle is succesfully installed in your local maven. 

![obrazek](https://user-images.githubusercontent.com/62996347/211896196-6a7d3c6e-2410-4315-91f8-1a699655176c.png)

### In your gradle project of choice add it to build.gradle id("me.chrommob.skidfuscatorgradle") version "1.0.2"

1. Your setting.gradle like this.

![obrazek](https://user-images.githubusercontent.com/62996347/211897065-7efdb6a8-9135-4e7e-b58a-45f66edb724f.png)

2. Your build.gradle should look something like this.

![obrazek](https://user-images.githubusercontent.com/62996347/212709866-12e8d695-9aeb-4bf1-9cc7-3f73a4b948ef.png)

3. Optional: If you want to customize how deep we search dependenices configure it using maxDepth variable. The default value is 3 and you should only increase this if you get missing libraries warning.

![obrazek](https://user-images.githubusercontent.com/62996347/212710350-d5a1457a-d45c-45e5-a118-3a6f4f56da88.png)

### In the project dir create skidfuscator folder and put skidfuscator.jar into it.
1. Create folder skifuscator in your project.
2. Download latest jar from https://github.com/skidfuscatordev/skidfuscator-java-obfuscator/releases/.
3. Rename the downloaded file to skidfuscator.jar and move it to the folder we created.

![obrazek](https://user-images.githubusercontent.com/62996347/211897648-96f86a39-ed47-42ab-8495-2c36c69cf7b5.png)

4. If you want to use exclusion put config.txt into the skidfuscator folder. 

### Compile your project like you would always do.
1. Run skidfuscate task.
2. In intellij you can use the gradle menu on the right.

![obrazek](https://user-images.githubusercontent.com/62996347/211897841-15481a45-5914-49ef-b2e1-803cf9302366.png)

3. If you do not use intellij run gradlew.bat skidfuscate on Windows or gradlew skidfuscate on MacOS and Linux.

### Obfuscation should complete and there should be new folder inside the skidfuscator folder with the obfuscated files.
1. The folder obfuscated jar is in project_location/skidfuscator/name_of_original_jar-/name_of_original_jar-out.jar
