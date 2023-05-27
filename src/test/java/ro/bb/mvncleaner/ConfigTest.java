package ro.bb.mvncleaner;

import org.junit.jupiter.api.Test;

public class ConfigTest {

    @Test
    void retrieveMvnRepo() {
        System.out.println(Config.repositoryRoot());
    }
}
