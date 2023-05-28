package ro.bb.mvncleaner;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Config {
    private static final Config ourInstance = new Config();

    Properties properties = new Properties();

    private Config() {
        try (InputStream streamProperties = getClass().getResourceAsStream("application.properties")) {
            properties.load(streamProperties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public static String repositoryRoot() {
        return ourInstance.properties.getProperty("repository.root");
    }

    public static boolean simulation() {
        return Boolean.parseBoolean(ourInstance.properties.getProperty("simulation"));
    }

    public static boolean cleanReleases() {
        return Boolean.parseBoolean(ourInstance.properties.getProperty("release.clean"));
    }
    public static boolean keepLastRelease() {
        return Boolean.parseBoolean(ourInstance.properties.getProperty("release.keep.last"));
    }
    public static boolean cleanSnapshots() {
        return Boolean.parseBoolean(ourInstance.properties.getProperty("snapshot.clean"));
    }
    public static boolean keepLastSnapshot() {
        return Boolean.parseBoolean(ourInstance.properties.getProperty("snapshot.keep.last"));
    }
    public static List<String> prefixesToProcess() {
        String property = ourInstance.properties.getProperty("prefix.to.process");
        if (property == null) return Collections.singletonList(""); // any string begins with ""
        return Arrays.asList(property.split(";", -1));
    }
    public static List<String> prefixesToSkip() {
        String property = ourInstance.properties.getProperty("prefix.to.skip");
        if (property == null || property.trim().length() == 0) { // nothing to skip
            return Collections.emptyList(); // nothing to skip
        }
        return Arrays.asList(property.split(";", -1));
    }

    /** Gives the files corresponding to the effective-POMs of the projects we want to keep the dependencies for */
    public static List<String> dependencyTreeFiles() {
        String property = ourInstance.properties.getProperty("dependencies.to.keep");
        if (property == null || property.trim().length() == 0) { // nothing to skip
            return Collections.emptyList(); // nothing to skip
        }
        return Arrays.asList(property.split(";"));
    }




    /* test */
    public static void main(String[] args) {
        System.out.println("repository root = " + Config.repositoryRoot());
        System.out.println("cleanSnapshots = " + Config.cleanSnapshots());
        System.out.println("keepLastSnapshot = " + Config.keepLastSnapshot());
        System.out.println("cleanReleases = " + Config.cleanReleases());
        System.out.println("keepLastRelease = " + Config.keepLastRelease());

        List<String> prefixesToProcess = Config.prefixesToProcess();
        System.out.println("Prefixes to process:");
        for (String p : prefixesToProcess) System.out.println("    " + p);

        List<String> prefixesToSkip = Config.prefixesToSkip();
        if (!prefixesToSkip.isEmpty()) {
            System.out.println("Prefixes to skip:");
            for (String p : prefixesToSkip) System.out.println("    " + p);
        } else {
            System.out.println("(No prefix to skip)");
        }

        if (Config.simulation()) System.out.println("Only simulate processing.");
    }
}
