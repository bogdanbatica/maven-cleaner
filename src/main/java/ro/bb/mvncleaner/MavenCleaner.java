package ro.bb.mvncleaner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MavenCleaner {

    private final int rootPathNameCount;
    Path repoRoot;

    public MavenCleaner(Path repoRoot) {
        this.repoRoot = repoRoot;
        rootPathNameCount = repoRoot.getNameCount();
    }

    public void go() throws Exception {
        for (File file : repoRoot.toFile().listFiles()) {
            if (file.isDirectory()) { // there may also be files in the repo root`
                trtDir(file.toPath());
            }
        }
    }

    /** processing of a directory in the local Maven repository.
     * If this directory contains other artifact directories, the processing delegates to them.
     * If this directory is an artifact directory, remove the obsolete directory versions
     * TODO classifiers
     * @param currentPath current directory to process
     */
    public void trtDir(Path currentPath) throws Exception {
        List<Path> entries = Files.list(currentPath).collect(Collectors.toList());
        boolean fileFound = false, versionDirFound = false, artifactDirFound = false;
        List<Path> versionDirs = new ArrayList<>(entries.size());
        for (Path dirEntry : entries) {
            if (dirEntry.toFile().isFile()) {
                fileFound = true;
            } else {
                if (Utils.isVersionDirectory(dirEntry.getFileName().toString())) {
                    versionDirFound = true;
                    versionDirs.add(dirEntry);
                } else { // by elimination...
                    artifactDirFound = true;
                }
            }
        }
        if ((versionDirFound && artifactDirFound) || (fileFound && !versionDirFound)) {
            // let's say it's normal to find some files (resolver-status.properties, maven-metadata-central.xml...) alongside the version directories
            System.out.println("ERROR: unexpected contents in " + currentPath);
            return;
        }
        if (versionDirFound) {
            cleanVersions(currentPath, versionDirs);
        } else {
            for (Path subdir : entries) {
                trtDir(subdir);
            }
        }
    }


    void cleanVersions(Path artifactPath, List<Path> versionDirs) throws IOException {
        int thisPathNameCount = artifactPath.getNameCount();
        String mvnPath = artifactPath.subpath(rootPathNameCount, thisPathNameCount)
                .toString().replace(File.separatorChar, '.');
        if (Config.prefixesToProcess().stream().anyMatch(mvnPath::startsWith)
                        && Config.prefixesToSkip().stream().noneMatch(mvnPath::startsWith)) {
            System.out.println("Cleaning " + artifactPath + "...");
            ArtifactVersionsCleaner cleaner = new ArtifactVersionsCleaner(artifactPath, versionDirs);
            cleaner.go();
        } else {
            System.out.println("Skipping " + artifactPath + "...");
        }
    }

    public static void main(String[] args) throws Exception {
        MavenCleaner cleaner = new MavenCleaner(Paths.get(Config.repositoryRoot()));

        cleaner.go();
    }
}