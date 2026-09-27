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

final class SqlScriptRunner {
    private SqlScriptRunner() { }

    static void execute(DatabaseManager databaseManager, String resource, String action) {
        String script = read(resource);
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String command : script.split(";")) {
                    if (!command.trim().isEmpty()) statement.execute(command);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(action + ": " + e.getMessage(), e);
        }
    }

    private static String read(String resource) {
        try (InputStream input = SqlScriptRunner.class.getResourceAsStream(resource)) {
            if (input == null) throw new DataAccessException("Файл " + resource + " не найден.");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new DataAccessException("Не удалось прочитать " + resource + ".", e);
        }
    }
}
