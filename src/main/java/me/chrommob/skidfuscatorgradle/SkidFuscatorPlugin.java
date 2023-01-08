package me.chrommob.skidfuscatorgradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * A plugin that obfuscates the project's jar file using SkidFuscator.
 */
public class SkidFuscatorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("skidfuscate", SkidFuscatorTask.class);
    }
}