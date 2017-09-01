package fts_api;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FTSProperties {
    private static Properties instance = new Properties();

    private static final String PROPERTIES_FILE_NAME = "fts.properties";

    static {
        reloadProperties();
    }

    public static void reloadProperties() {
        try {
            instance.load(new FileInputStream(PROPERTIES_FILE_NAME));
        } catch (IOException e) {
            System.err.println("Unable to read properties from file " + PROPERTIES_FILE_NAME + ": " + e.getMessage());
        }
    }

    public static Properties getInstance() {
        return instance;
    }

    private FTSProperties() {
    }
}
