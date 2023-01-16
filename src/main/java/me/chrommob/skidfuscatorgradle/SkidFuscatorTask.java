package me.chrommob.skidfuscatorgradle;

import org.gradle.api.provider.Property;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public abstract class SkidFuscatorTask extends DefaultTask {
    @Input
    @Optional
    abstract public Property<Integer> getMaxDepth();
    private final File skidfuscatorJar = new File(getProject().getProjectDir() + File.separator + "skidfuscator", "skidfuscator.jar");
    private final File skidfuscatorFolder = new File(getProject().getProjectDir() + File.separator + "skidfuscator");
    private final File mavenRepo = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
    private final File exclusionFile = new File(skidfuscatorFolder, "exclusions.txt");

    @TaskAction
    /**
     * Runs the obfuscation.
     */
    public void run() {
        int maxDepth = 0;
        if (getMaxDepth().isPresent()) {
            if (getMaxDepth().get().equals(0)) {
               throw new IllegalArgumentException("Max depth cannot be 0.");
            }
            maxDepth = getMaxDepth().get();
        } else {
            maxDepth = 3;
        }
        System.out.println("Max depth: " + maxDepth);
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
        DependencyFinder dependencyFinder = new DependencyFinder(mavenRepo, skidfuscatorFolder, maxDepth);
        Set<File> compileLibs = getProject().getConfigurations().getByName("compileClasspath").getFiles();
        Set<org.gradle.api.artifacts.Dependency> dependencies = new HashSet<>(getProject().getConfigurations().getByName("compileClasspath").getAllDependencies());
        for (Project project : getProject().getAllprojects()) {
            if (project.getConfigurations().getByName("compileClasspath").isCanBeResolved()) {
                try {
                    dependencies.addAll(project.getConfigurations().getByName("compileClasspath").getAllDependencies());
                    compileLibs.addAll(project.getConfigurations().getByName("compileClasspath").getFiles());
                } catch (Exception ignored) {}
            }
        }
        Project parent = getProject().getParent();
        while (parent != null) {
            for (Project project : parent.getAllprojects()) {
                if (project.getConfigurations().getByName("compileClasspath").isCanBeResolved()) {
                    try {
                        dependencies.addAll(project.getConfigurations().getByName("compileClasspath").getAllDependencies());
                        compileLibs.addAll(project.getConfigurations().getByName("compileClasspath").getFiles());
                    } catch (Exception ignored) {
                    }
                }
            }
            parent = parent.getParent();
        }
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
        File manualLibs = new File(skidfuscatorFolder + File.separator + "manualLibs");
        if (manualLibs.listFiles() != null && Objects.requireNonNull(manualLibs.listFiles()).length > 0) {
            for (File lib : Objects.requireNonNull(manualLibs.listFiles())) {
                try {
                    Files.copy(lib.toPath(), new File(skidfuscatorFolder + File.separator + "libs" + File.separator + lib.getName()).toPath());
                } catch (IOException ignored) {
                }
            }
        }
        for (File lib: Objects.requireNonNull(new File(skidfuscatorFolder + File.separator + "libs").listFiles())) {
            if (lib.getName().endsWith(".jar")) {
                try {
                    ZipFile zipFile = new ZipFile(lib);
                    //Check if the jar is a valid jar
                   ZipEntry entry = zipFile.getEntry("annotations");
                    if (entry != null) {
                        System.out.println("Removing " + lib.getName() + " because it is has annotations.");
                        zipFile.close();
                        Files.delete(lib.toPath());
                    }
                    zipFile.close();
                } catch (ZipException e) {
                    System.out.println("Deleting " + lib.getName() + " because it is not a valid jar.");
                    try {
                        Files.delete(lib.toPath());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
        try {
            Files.delete(directoryToBeDeleted.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
