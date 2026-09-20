package ru.pkc.travel.util;

import ru.pkc.travel.exception.DataAccessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class SchemaInitializer {
    private SchemaInitializer() { }

    public static void initialize(DatabaseManager databaseManager) {
        String script = readScript();
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            for (String command : script.split(";")) {
                if (!command.trim().isEmpty()) {
                    statement.execute(command);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка создания схемы базы данных: " + e.getMessage(), e);
        }
    }

    private static String readScript() {
        try (InputStream input = SchemaInitializer.class.getResourceAsStream("/schema.sql")) {
            if (input == null) {
                throw new DataAccessException("Файл schema.sql не найден.");
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new DataAccessException("Не удалось прочитать schema.sql.", e);
        }
    }
}
