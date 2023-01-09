package me.chrommob.skidfuscatorgradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class SkidFuscatorTask extends DefaultTask {
    private final File skidfuscatorJar = new File(getProject().getProjectDir() + File.separator + "skidfuscator", "skidfuscator.jar");
    private final File skidfuscatorFolder = new File(getProject().getProjectDir() + File.separator + "skidfuscator");
    private final File mavenRepo = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
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
        for (File file : Objects.requireNonNull(skidfuscatorFolder.listFiles())) {
            if (!file.getName().equals("skidfuscator.jar") && !file.getName().equals("manualLibs")) {
                deleteDirectory(file);
            }
        }
        DependencyFinder dependencyFinder = new DependencyFinder(mavenRepo, skidfuscatorFolder);
        Set<File> compileLibs = getProject().getConfigurations().getByName("compileClasspath").getFiles();
        Set<File> depJars = getProject().getConfigurations().getByName("compileClasspath").getFiles();
        for (File depJar : depJars) {
            File parent = depJar.getParentFile();
            if (parent == null)
                continue;
            File version = parent.getParentFile();
            if (version == null)
                continue;
            File artifact = version.getParentFile();
            if (artifact == null)
                continue;
            File group = artifact.getParentFile();
            if (group == null)
                continue;
            System.out.println("Found dependency: " + group.getName() + ":" + artifact.getName() + ":" + version.getName());
            Dependency dep = new Dependency(dependencyFinder, group.getName(), artifact.getName(), version.getName(), Collections.singleton("https://repo1.maven.org/maven2/"));
            compileLibs.addAll(dep.getFiles());
        }
        new File(skidfuscatorFolder + File.separator + "libs").mkdirs();
        for (File lib : compileLibs) {
            if (lib == null)
                continue;
            try {
                Files.copy(lib.toPath(), new File(skidfuscatorFolder + File.separator + "libs" + File.separator + lib.getName()).toPath());
            } catch (IOException ignored) {
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

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
