package ru.pkc.travel;

import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.EmployeeRepository;
import ru.pkc.travel.repository.jdbc.JdbcBusinessTripRepository;
import ru.pkc.travel.repository.jdbc.JdbcEmployeeRepository;
import ru.pkc.travel.service.BusinessTripService;
import ru.pkc.travel.service.EmployeeService;
import ru.pkc.travel.ui.ConsoleApplication;
import ru.pkc.travel.ui.InputReader;
import ru.pkc.travel.util.DatabaseManager;
import ru.pkc.travel.util.ExcelExporter;
import ru.pkc.travel.util.SchemaInitializer;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        configureUtf8Output();
        try {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.testConnection();
            SchemaInitializer.initialize(databaseManager);

            EmployeeRepository employeeRepository = new JdbcEmployeeRepository(databaseManager);
            BusinessTripRepository tripRepository = new JdbcBusinessTripRepository(databaseManager);
            EmployeeService employeeService = new EmployeeService(employeeRepository, tripRepository);
            BusinessTripService tripService = new BusinessTripService(tripRepository, employeeRepository);

            System.out.println("Подключение к PostgreSQL установлено. Схема базы данных готова.");
            new ConsoleApplication(new InputReader(System.in), employeeService, tripService,
                    new ExcelExporter()).run();
        } catch (DataAccessException e) {
            System.err.println("Запуск невозможен: " + e.getMessage());
            System.err.println("Проверьте PostgreSQL и параметры DB_URL, DB_USER, DB_PASSWORD.");
        }
    }

    private static void configureUtf8Output() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {
            // The application remains usable even if the console does not support UTF-8.
        }
    }
}
