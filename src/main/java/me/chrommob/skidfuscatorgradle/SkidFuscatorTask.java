package me.chrommob.skidfuscatorgradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class SkidFuscatorTask extends DefaultTask {
    private final File skidfuscatorJar = new File(getProject().getProjectDir() + File.separator + "skidfuscator", "skidfuscator.jar");
    private final File skidfuscatorFolder = new File(getProject().getProjectDir() + File.separator + "skidfuscator");
    private final File mavenRepo = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
    private final File exclusionFile = new File(skidfuscatorFolder, "exclusions.txt");
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
            if (!file.getName().equals("skidfuscator.jar") && !file.getName().equals("manualLibs") && !file.getName().equals("exclusions.txt")) {
                deleteDirectory(file);
            }
        }
        DependencyFinder dependencyFinder = new DependencyFinder(mavenRepo, skidfuscatorFolder);
        Set<File> compileLibs = getProject().getConfigurations().getByName("compileClasspath").getFiles();
        DependencySet dependencies = getProject().getConfigurations().getByName("compileClasspath").getAllDependencies();
        compileLibs.addAll(getProject().getConfigurations().getByName("compileClasspath").getFiles());
        for (org.gradle.api.artifacts.Dependency dependency : dependencies) {
            System.out.println("Found dependency: " + dependency.getGroup() + ":" + dependency.getName() + ":" + dependency.getVersion());
            Dependency dep = new Dependency(dependencyFinder, dependency.getGroup(), dependency.getName(), dependency.getVersion(), Collections.singleton("https://repo1.maven.org/maven2/"));
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
        for (File lib: Objects.requireNonNull(new File(skidfuscatorFolder + File.separator + "libs").listFiles())) {
            if (lib.getName().endsWith(".jar")) {
                try {
                    new ZipFile(lib).close();
                } catch (ZipException e) {
                    System.out.println("Deleting " + lib.getName() + " because it is not a valid jar.");
                    lib.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            List<String> args = new ArrayList<>();
            args.add(new File(outputFolder + File.separator + outPutFile.getName()).getAbsolutePath());
            args.add("-li=" + new File(skidfuscatorFolder + File.separator + "libs").getAbsolutePath());
            if (exclusionFile.exists()) {
                args.add("-ex=" + exclusionFile.getAbsolutePath());
            }
            javaExec.setArgs(args);
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
