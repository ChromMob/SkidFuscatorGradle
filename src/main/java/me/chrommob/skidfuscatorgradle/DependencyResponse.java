package me.chrommob.skidfuscatorgradle;

import java.io.File;
import java.util.Set;

public class DependencyResponse {
    private final File dependency;
    private final Set<Dependency> subDependencies;

    public DependencyResponse(File dependency, Set<Dependency> subDependencies) {
        this.dependency = dependency;
        this.subDependencies = subDependencies;
    }

    public File getDependency() {
        return dependency;
    }

    public Set<Dependency> getSubDependencies() {
        return subDependencies;
    }
}
