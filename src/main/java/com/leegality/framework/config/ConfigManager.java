
package com.leegality.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Properties PROPERTIES = loadProperties();

    private static Properties loadProperties() {

        Properties properties = new Properties();

        try (InputStream input =
                     ConfigManager.class.getClassLoader()
                             .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "config.properties file was not found"
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load configuration file", e
            );
        }

        return properties;
    }

    public static String getBaseUrl() {
        return PROPERTIES.getProperty("base.url");
    }
}
