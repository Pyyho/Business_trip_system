package ru.pkc.travel.util;

public final class DemoDataInitializer {
    private DemoDataInitializer() { }

    public static void initialize(DatabaseManager databaseManager) {
        SqlScriptRunner.execute(databaseManager, "/demo-data.sql", "Ошибка загрузки демонстрационных данных");
    }
}
