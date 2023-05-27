package ro.bb.mvncleaner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/** Central processing class for cleaning the versions of a specific Maven artifact in the local repository */
public class ArtifactVersionsCleaner {

    Path artifactPath;

    TreeSet<ArtifactVersion> versions;

    public ArtifactVersionsCleaner(Path artifactPath, List<Path> versionPaths) {
        this.artifactPath = artifactPath;
        versions = new TreeSet<>();
        versionPaths.forEach(p -> versions.add(new ArtifactVersion(p)));
    }

    public void go() throws IOException {
        Iterator<ArtifactVersion> iterator = versions.descendingIterator();
        boolean releaseFound = false, snapshotFound = false;
        while (iterator.hasNext()) {
            ArtifactVersion version = iterator.next();
            if (version.isSnapshot()) {
                if (Config.cleanSnapshots() && (snapshotFound || !Config.keepLastSnapshot())) {
                    removeVersionDir(version.versionPath);
                }
                snapshotFound = true;
            } else {
                if (Config.cleanReleases() && (releaseFound || !Config.keepLastRelease())) {
                    removeVersionDir(version.versionPath);
                }
                releaseFound = true;
            }
        }
    }

    void removeVersionDir(Path dirPath) throws IOException {
        Files.list(dirPath).forEach(this::removeFile);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /* test */
    public static void main(String[] args) throws IOException {
        Path artifactPath = Paths.get("m:\\test\\.m2\\org\\springframework\\spring-core\\");
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
        ArtifactVersionsCleaner obj = new ArtifactVersionsCleaner(artifactPath, versionPaths);
        obj.go();
    }
}
