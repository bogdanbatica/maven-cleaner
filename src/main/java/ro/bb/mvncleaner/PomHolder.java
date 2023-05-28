package ro.bb.mvncleaner;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PomHolder {

    /** key = groupId.artifactId, value = collection of versions */
    Map<String, Set<String>> dependencyVersions = new HashMap<>();


    public static PomHolder createFrom(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.isFile()) return null;
        try (
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader))
        {
            MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
            Model model = mavenXpp3Reader.read(reader);
            return new PomHolder(model);
        }
    }

    private PomHolder(Model model) {
        model.getDependencies().forEach(this::addDependencyVersion);
        model.getBuild().getPlugins().forEach(plugin ->
                plugin.getDependencies().forEach(this::addDependencyVersion) // in case they weren't in the list above
        );
    }

    void addDependencyVersion(Dependency dep) {
        String artifactKey = dep.getGroupId() + "." + dep.getArtifactId();
        String version = dep.getVersion();
        dependencyVersions.computeIfAbsent(artifactKey, k -> new HashSet<>()).add(version);
    }



    private static Map<String, Set<String>> allDependencyVersions;
    private static Map<String, Set<String>> getAllDependencyVersions() throws Exception {
        if (allDependencyVersions != null) return allDependencyVersions;

        allDependencyVersions = new HashMap<>();
        for (String effectivePomPath : Config.pomFiles()) {
            PomHolder pomHolder = createFrom(effectivePomPath); if (pomHolder == null) continue;
            pomHolder.dependencyVersions.forEach((artifactKey, versions) ->
                allDependencyVersions.computeIfAbsent(artifactKey, k -> new HashSet<>())
                        .addAll(versions)
            );
        }
        return allDependencyVersions;
    }

    public static boolean artifactVersionToKeep(String artifactKey, String version) throws Exception {
        Set<String> versions = getAllDependencyVersions().get(artifactKey);
        return (versions != null) && versions.contains(version);
    }



    /* test */
    public static void main(String[] args) throws Exception {
        PomHolder pomHolder = createFrom("M:\\javaOutils-effective-pom.xml");
        pomHolder.dependencyVersions.forEach((artifactKey, versions) ->
            System.out.println(artifactKey + " : " + String.join(", ", versions))
        );
    }

}
