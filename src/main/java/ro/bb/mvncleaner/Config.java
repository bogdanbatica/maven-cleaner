package ro.bb.mvncleaner;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Config {
    private static Config ourInstance = new Config();

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


}
