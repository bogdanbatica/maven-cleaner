package ro.bb.mvncleaner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dependency info as rendered by mvn dependency:tree */
public class MavenDependencyTree {

    public static final Pattern ARTIFACT_VERSION_PATTERN = Pattern.compile(" ([^ :]+):([^ :]+):([^ :]+):([^ :]+):([^ :]+)");

    /** key = groupId.artifactId, value = collection of versions */
    Map<String, Set<String>> dependencyVersions = new HashMap<>();

    /**
     * Reads a file obtained as output of the command mvn dependency:tree.
     * The lines of interest contain the concatenation groupId:artifactId:packaging:version:scope
     */
    public MavenDependencyTree(String filePath) throws Exception {
        try (
                FileReader fileReader = new FileReader(filePath);
                BufferedReader buf = new BufferedReader(fileReader)) {
            String line = buf.readLine();
            while (line != null) {
                Matcher matcher = ARTIFACT_VERSION_PATTERN.matcher(line);
                if (matcher.find()) {
                    String artifactKey = matcher.group(1) + '.' + matcher.group(2);
                    String version = matcher.group(4);
                    dependencyVersions.computeIfAbsent(artifactKey, k -> new HashSet<>()).add(version);
                }
                line = buf.readLine();
            }
        }
    }



    private static Map<String, Set<String>> allDependencyVersions;
    private static Map<String, Set<String>> getAllDependencyVersions() throws Exception {
        if (allDependencyVersions != null) return allDependencyVersions;

        allDependencyVersions = new HashMap<>();
        for (String depTreePath : Config.dependencyTreeFiles()) {
            if (!Files.isRegularFile(Paths.get(depTreePath))) continue;
            MavenDependencyTree depTree = new MavenDependencyTree(depTreePath);
            depTree.dependencyVersions.forEach((artifactKey, versions) ->
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
        String line = "[INFO] |  |  +- org.springframework.boot:spring-boot:jar:2.5.5:compile";
        Matcher matcher = ARTIFACT_VERSION_PATTERN.matcher(line);
        System.out.println(matcher.find());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        System.out.println(matcher.group(4));

        MavenDependencyTree depTree = new MavenDependencyTree("C:\\b\\IdeaWsp\\FormationVL\\formationAndrada\\dep.txt");
        depTree.dependencyVersions.forEach((artifactKey, versions) ->
                System.out.println(artifactKey + " : " + String.join(", ", versions))
        );

    }
}
