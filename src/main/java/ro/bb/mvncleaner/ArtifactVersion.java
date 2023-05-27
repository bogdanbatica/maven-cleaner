package ro.bb.mvncleaner;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.nio.file.Path;

public class ArtifactVersion implements Comparable<ArtifactVersion> {

    Path versionPath;

    ComparableVersion comparableVersion;

    public ArtifactVersion(Path versionPath) {
        this.versionPath = versionPath;
        comparableVersion = new ComparableVersion(versionPath.getFileName().toString());
    }

    @Override
    public int compareTo(ArtifactVersion theOther) {
        return this.comparableVersion.compareTo(theOther.comparableVersion);
        // let it crash if null value, won't happen
    }

    public boolean isSnapshot() {
        return comparableVersion.toString().toUpperCase().endsWith("SNAPSHOT");
    }
}
