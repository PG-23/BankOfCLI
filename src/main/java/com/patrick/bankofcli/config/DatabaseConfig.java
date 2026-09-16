package com.patrick.bankofcli.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class
                .getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("db.properties not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("DB_URL");
    }

    public static String getUser() {
        return properties.getProperty("DB_USER");
    }

    public static String getPassword() {
        return properties.getProperty("DB_PASSWORD");
    }
}