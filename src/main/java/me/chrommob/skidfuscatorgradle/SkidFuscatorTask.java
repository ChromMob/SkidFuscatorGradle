package me.chrommob.skidfuscatorgradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class SkidFuscatorTask extends DefaultTask {
    private final File skidfuscatorJar = new File(getProject().getProjectDir() + File.separator + "skidfuscator", "skidfuscator.jar");
    private final File skidfuscatorFolder = new File(getProject().getProjectDir() + File.separator + "skidfuscator");
    @TaskAction
    /**
     * Runs the obfuscation.
     */
    public void run() {
        if (!skidfuscatorJar.exists())
            throw new RuntimeException("Skifuscator not found in: " + skidfuscatorJar.getAbsolutePath());
        File output = new File(getProject().getProjectDir() + File.separator + "build" + File.separator + "libs");
        if (output.listFiles() == null || Objects.requireNonNull(output.listFiles()).length == 0) {
            throw  new RuntimeException("No output file to obfuscate.");
        }
        for (File file : skidfuscatorFolder.listFiles()) {
            if (!file.getName().equals("skidfuscator.jar")) {
                try {
                    Files.walk(file.toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Set<File> compileLibs = getProject().getConfigurations().getByName("compileClasspath").getFiles();
        new File(skidfuscatorFolder + File.separator + "libs").mkdirs();
        for (File lib : compileLibs) {
            try {
                Files.copy(lib.toPath(), new File(skidfuscatorFolder + File.separator + "libs" + File.separator + lib.getName()).toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (File outPutFile : Objects.requireNonNull(output.listFiles())) {
            String name = outPutFile.getName().replaceAll(".jar", "");
            File outputFolder = new File(skidfuscatorFolder + File.separator + name);
            outputFolder.mkdirs();
            try {
                Files.copy(outPutFile.toPath(), new File(outputFolder + File.separator + outPutFile.getName()).toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Obfuscating: " + outPutFile.getName());

            JavaExec javaExec = getProject().getTasks().create("run" + outPutFile.getName().replaceAll(".jar", ""), JavaExec.class);
            javaExec.setWorkingDir(outputFolder);
            javaExec.getAllJvmArgs().add("-jar");
            javaExec.setArgs(List.of(new File(outputFolder + File.separator + outPutFile.getName()).getAbsolutePath(), "-li=" + new File(skidfuscatorFolder + File.separator + "libs")));
            javaExec.setClasspath(getProject().files().from(skidfuscatorJar));
            javaExec.exec();
            System.out.println("File successfully obfuscated.");
        }
    }
}
