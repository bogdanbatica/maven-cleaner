package ro.bb.mvncleaner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

/** Central processing class for cleaning the versions of a specific Maven artifact in the local repository */
public class ArtifactVersionsCleaner {

    String artifactKey;

    TreeSet<ArtifactVersion> versions;

    public ArtifactVersionsCleaner(String artifactKey, List<Path> versionPaths) {
        this.artifactKey = artifactKey;
        versions = new TreeSet<>();
        versionPaths.forEach(p -> versions.add(new ArtifactVersion(p)));
    }

    public void go() throws Exception {
        Iterator<ArtifactVersion> iterator = versions.descendingIterator();
        boolean releaseFound = false, snapshotFound = false;
        while (iterator.hasNext()) {
            ArtifactVersion artifactVersion = iterator.next();
            if (artifactVersion.isSnapshot()) {
                if (Config.cleanSnapshots() && (snapshotFound || !Config.keepLastSnapshot())) {
                    removeVersionDir(artifactVersion);
                }
                snapshotFound = true;
            } else {
                if (Config.cleanReleases() && (releaseFound || !Config.keepLastRelease())) {
                    removeVersionDir(artifactVersion);
                }
                releaseFound = true;
            }
        }
    }

    void removeVersionDir(ArtifactVersion artifactVersion) throws Exception {
        Path dirPath = artifactVersion.versionPath;
        String version = artifactVersion.comparableVersion.toString();
        if (PomHolder.artifactVersionToKeep(artifactKey, version)) {
            System.out.println("    Skipping version " + version);
            return;
        }
        try (Stream<Path> pathStream = Files.list(dirPath)) {
            pathStream.forEach(this::removeFile);
        }
        removeFile(dirPath);
    }

    void removeFile(Path filePath) {
        if (Config.simulation()) {
//            System.out.println("    To delete " + filePath);
            return;
        }
//        System.out.println("    Deleting " + filePath);
        try {
            Files.delete(filePath);
        } catch (Exception e) {
            System.out.println("    ERROR: couldn't delete " + filePath);
        }
    }


    /* test */
    public static void main(String[] args) throws Exception {
        String artifactKey = "org.springframework.spring-core";
        List<Path> versionPaths = Arrays.asList(
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\5.1.3.RELEASE"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\5.3.10"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\5.3.13"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\5.3.14"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\5.3.16"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\5.3.24"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\6.0.2"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\6.0.3"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\6.0.6"),
                Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\6.0.8")
        );
        ArtifactVersionsCleaner obj = new ArtifactVersionsCleaner(artifactKey, versionPaths);
        obj.go();
    }
}
