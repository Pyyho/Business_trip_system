package ru.pkc.travel.util;

import ru.pkc.travel.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;
    private final boolean demoDataEnabled;

    public DatabaseManager() {
        Properties properties = loadProperties();
        url = getSetting("DB_URL", properties, "db.url");
        user = getSetting("DB_USER", properties, "db.user");
        password = getSetting("DB_PASSWORD", properties, "db.password");
        demoDataEnabled = getBooleanSetting("APP_SEED_DEMO", properties, "app.seed-demo", true);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean isDemoDataEnabled() {
        return demoDataEnabled;
    }

    public void testConnection() {
        try (Connection ignored = getConnection()) {
            // try-with-resources guarantees that the test connection is closed.
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось подключиться к PostgreSQL: " + e.getMessage(), e);
        }
    }

    private static String getSetting(String environmentName, Properties properties, String key) {
        String environmentValue = System.getenv(environmentName);
        String value = environmentValue == null || environmentValue.trim().isEmpty()
                ? properties.getProperty(key) : environmentValue;
        if (value == null || value.trim().isEmpty()) {
            throw new DataAccessException("Не задан параметр базы данных: " + key);
        }
        return value.trim();
    }

    private static boolean getBooleanSetting(String environmentName, Properties properties,
                                             String key, boolean defaultValue) {
        String environmentValue = System.getenv(environmentName);
        String value = environmentValue == null || environmentValue.trim().isEmpty()
                ? properties.getProperty(key) : environmentValue;
        return value == null || value.trim().isEmpty() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new DataAccessException("Файл application.properties не найден.");
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new DataAccessException("Не удалось прочитать application.properties.", e);
        }
    }
}
