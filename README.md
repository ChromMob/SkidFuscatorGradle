# SkidFuscatorGradle
## How to use?
1. Clone this repo and publish it to local maven.
1.1
![obrazek](https://user-images.githubusercontent.com/62996347/211895035-c32db81d-98b8-4d6e-bad9-9010b0c0077e.png)
1.2 Extract the zip.
![obrazek](https://user-images.githubusercontent.com/62996347/211895609-336a364f-115b-41e8-9684-86e80c5d6f6e.png)
1.3 Open CMD or Terminal linux inside the SkidFuscatorGradle folder and execute gradlew.bat publishToMavenLocal on Windows systems. On MacOS and Linux run gradlew publishToMavenLocal.
1.4 SkidFuscatorGradle is succesfully installed in your local maven. 
![obrazek](https://user-images.githubusercontent.com/62996347/211896196-6a7d3c6e-2410-4315-91f8-1a699655176c.png)
2. In your gradle project of choice add it to build.gradle id("me.chrommob.skidfuscatorgradle") version "1.0"
2.1 Your build.gradle should look something like this.
![obrazek](https://user-images.githubusercontent.com/62996347/211896962-a3aebde7-1e44-4fa5-af84-de7a8ecb85fc.png)
2.2 Your setting.gradle like this.
![obrazek](https://user-images.githubusercontent.com/62996347/211897065-7efdb6a8-9135-4e7e-b58a-45f66edb724f.png)
3. In the project dir create skidfuscator folder and put skidfuscator.jar into it.
3.1 Create folder skifuscator in your project.
3.2 Download latest jar from https://github.com/skidfuscatordev/skidfuscator-java-obfuscator/releases/.
3.3 Rename the downloaded file to skidfuscator.jar and move it to the folder we created.
![obrazek](https://user-images.githubusercontent.com/62996347/211897648-96f86a39-ed47-42ab-8495-2c36c69cf7b5.png)
4. Compile your project like you would always do.
5. Run skidfuscate task.
5.1 In intellij you can use the gradle menu on the right.
![obrazek](https://user-images.githubusercontent.com/62996347/211897841-15481a45-5914-49ef-b2e1-803cf9302366.png)
5.2 If you do not use intellij run gradlew.bat skidfuscate on Windows or gradlew skidfuscate on MacOS and Linux.
6. Obfuscation should complete and there should be new folder inside the skidfuscator folder with the obfuscated files.
6.1 The folder obfuscated jar is in project_location/skidfuscator/name_of_original_jar-/name_of_original_jar-out.jar
