package ru.puzpuzpuz.http.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A reader for files in .properties format.
 */
public class PropertiesReader {

    private final Properties properties;

    private PropertiesReader(Properties properties) {
        this.properties = properties;
    }

    public static PropertiesReader init(String settingsPath) throws IOException {
        try (
            InputStream inputStream = new FileInputStream(new File(settingsPath))
        ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return new PropertiesReader(properties);
        }
    }

    public Integer readIntKey(String key) {
        String keyVal = properties.getProperty(key);
        return keyVal != null ? Integer.valueOf(keyVal) : null;
    }

    public String readStringKey(String key) {
        return properties.getProperty(key);
    }

}
