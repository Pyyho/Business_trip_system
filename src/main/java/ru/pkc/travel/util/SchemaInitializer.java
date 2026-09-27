package ru.pkc.travel.util;

public final class SchemaInitializer {
    private SchemaInitializer() { }

    public static void initialize(DatabaseManager databaseManager) {
        SqlScriptRunner.execute(databaseManager, "/schema.sql", "Ошибка создания схемы базы данных");
    }
}
