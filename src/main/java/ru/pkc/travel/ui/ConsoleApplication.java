package ru.pkc.travel.ui;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.model.*;
import ru.pkc.travel.service.AccommodationService;
import ru.pkc.travel.service.BusinessTripService;
import ru.pkc.travel.service.EmployeeService;
import ru.pkc.travel.service.TransportTicketService;
import ru.pkc.travel.service.TripExpenseService;
import ru.pkc.travel.util.DataExporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsoleApplication {
    private final InputReader input;
    private final EmployeeService employeeService;
    private final BusinessTripService tripService;
    private final TransportTicketService ticketService;
    private final AccommodationService accommodationService;
    private final TripExpenseService expenseService;
    private final DataExporter exporter;

    public ConsoleApplication(InputReader input, EmployeeService employeeService,
                              BusinessTripService tripService, TransportTicketService ticketService,
                              AccommodationService accommodationService, TripExpenseService expenseService,
                              DataExporter exporter) {
        this.input = input;
        this.employeeService = employeeService;
        this.tripService = tripService;
        this.ticketService = ticketService;
        this.accommodationService = accommodationService;
        this.expenseService = expenseService;
        this.exporter = exporter;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            try {
                int action = input.readInt("Выберите действие: ");
                switch (action) {
                    case 1: employeeMenu(); break;
                    case 2: tripMenu(); break;
                    case 3: ticketMenu(); break;
                    case 4: accommodationMenu(); break;
                    case 5: expenseMenu(); break;
                    case 6: searchMenu(); break;
                    case 7: filterMenu(); break;
                    case 8: showStatistics(); break;
                    case 9: exportData(); break;
                    case 10: showDatabaseTables(); break;
                    case 0: running = false; break;
                    default: System.out.println("Неизвестный пункт меню.");
                }
            } catch (BusinessException | DataAccessException e) {
                printError(e.getMessage());
            } catch (RuntimeException e) {
                printError("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        System.out.println("Работа системы завершена.");
    }

    private void employeeMenu() {
        boolean open = true;
        while (open) {
            System.out.println("\n----- СОТРУДНИКИ -----");
            System.out.println("1. Добавить\n2. Вывести всех\n3. Найти по ID\n4. Изменить\n5. Удалить\n0. Назад");
            try {
                switch (input.readInt("Ваш выбор: ")) {
                    case 1: createEmployee(); break;
                    case 2: printEmployees(employeeService.findAll()); break;
                    case 3: System.out.println(employeeService.findById(input.readId("ID: "))); break;
                    case 4: updateEmployee(); break;
                    case 5: deleteEmployee(); break;
                    case 0: open = false; break;
                    default: System.out.println("Неизвестный пункт.");
                }
            } catch (BusinessException | DataAccessException e) { printError(e.getMessage()); }
        }
    }

    private void tripMenu() {
        boolean open = true;
        while (open) {
            System.out.println("\n----- СЛУЖЕБНЫЕ ПОЕЗДКИ -----");
            System.out.println("1. Создать\n2. Вывести все\n3. Найти по ID\n4. Изменить\n5. Изменить статус\n6. Удалить\n7. Сортировка\n0. Назад");
            try {
                switch (input.readInt("Ваш выбор: ")) {
                    case 1: createTrip(); break;
                    case 2: printTrips(tripService.findAll()); break;
                    case 3: printTripDetails(tripService.findById(input.readId("ID: "))); break;
                    case 4: updateTrip(); break;
                    case 5: changeTripStatus(); break;
                    case 6: deleteTrip(); break;
                    case 7: sortMenu(); break;
                    case 0: open = false; break;
                    default: System.out.println("Неизвестный пункт.");
                }
            } catch (BusinessException | DataAccessException e) { printError(e.getMessage()); }
        }
    }

    private void ticketMenu() {
        boolean open = true;
        while (open) {
            System.out.println("\n----- ТРАНСПОРТНЫЕ БИЛЕТЫ -----");
            System.out.println("1. Создать\n2. Вывести все\n3. Найти по ID\n4. Вывести по поездке\n5. Изменить\n6. Удалить\n0. Назад");
            try {
                switch (input.readInt("Ваш выбор: ")) {
                    case 1: createTicket(); break;
                    case 2: printTickets(ticketService.findAll()); break;
                    case 3: System.out.println(ticketService.findById(input.readId("ID: "))); break;
                    case 4: printTickets(ticketService.findByTripId(input.readId("ID поездки: "))); break;
                    case 5: updateTicket(); break;
                    case 6: deleteTicket(); break;
                    case 0: open = false; break;
                    default: System.out.println("Неизвестный пункт.");
                }
            } catch (BusinessException | DataAccessException e) { printError(e.getMessage()); }
        }
    }

    private void accommodationMenu() {
        boolean open = true;
        while (open) {
            System.out.println("\n----- ПРОЖИВАНИЕ -----");
            System.out.println("1. Создать\n2. Вывести все\n3. Найти по ID\n4. Вывести по поездке\n5. Изменить\n6. Удалить\n0. Назад");
            try {
                switch (input.readInt("Ваш выбор: ")) {
                    case 1: createAccommodation(); break;
                    case 2: printAccommodations(accommodationService.findAll()); break;
                    case 3: System.out.println(accommodationService.findById(input.readId("ID: "))); break;
                    case 4: printAccommodations(accommodationService.findByTripId(input.readId("ID поездки: "))); break;
                    case 5: updateAccommodation(); break;
                    case 6: deleteAccommodation(); break;
                    case 0: open = false; break;
                    default: System.out.println("Неизвестный пункт.");
                }
            } catch (BusinessException | DataAccessException e) { printError(e.getMessage()); }
        }
    }

    private void expenseMenu() {
        boolean open = true;
        while (open) {
            System.out.println("\n----- РАСХОДЫ -----");
            System.out.println("1. Создать\n2. Вывести все\n3. Найти по ID\n4. Вывести по поездке\n5. Изменить\n6. Удалить\n0. Назад");
            try {
                switch (input.readInt("Ваш выбор: ")) {
                    case 1: createExpense(); break;
                    case 2: printExpenses(expenseService.findAll()); break;
                    case 3: System.out.println(expenseService.findById(input.readId("ID: "))); break;
                    case 4: printExpenses(expenseService.findByTripId(input.readId("ID поездки: "))); break;
                    case 5: updateExpense(); break;
                    case 6: deleteExpense(); break;
                    case 0: open = false; break;
                    default: System.out.println("Неизвестный пункт.");
                }
            } catch (BusinessException | DataAccessException e) { printError(e.getMessage()); }
        }
    }

    private void searchMenu() {
        System.out.println("\n1. По месту назначения\n2. По ФИО сотрудника\n0. Назад");
        int action = input.readInt("Ваш выбор: ");
        if (action == 1) printTrips(tripService.searchByDestination(input.readRequired("Часть места: ")));
        else if (action == 2) printTrips(tripService.searchByEmployee(input.readRequired("Часть ФИО: ")));
        else if (action != 0) throw new BusinessException("Неизвестный пункт.");
    }

    private void filterMenu() {
        System.out.println("\n1. По статусу\n2. По пересечению с диапазоном дат\n0. Назад");
        int action = input.readInt("Ваш выбор: ");
        if (action == 1) printTrips(tripService.filterByStatus(input.readStatus("Статус: ")));
        else if (action == 2) {
            LocalDate from = input.readDate("Начало диапазона: ");
            LocalDate to = input.readDate("Конец диапазона: ");
            printTrips(tripService.filterByDates(from, to));
        } else if (action != 0) throw new BusinessException("Неизвестный пункт.");
    }

    private void sortMenu() {
        System.out.println("\n1. По дате начала (по возрастанию)\n2. По плановой стоимости (по убыванию)\n0. Назад");
        int action = input.readInt("Ваш выбор: ");
        if (action == 1) printTrips(tripService.sortByStartDate());
        else if (action == 2) printTrips(tripService.sortByPlannedCostDescending());
        else if (action != 0) throw new BusinessException("Неизвестный пункт.");
    }

    private void createEmployee() {
        Employee employee = employeeService.create(input.readRequired("ФИО: "),
                input.readRequired("Email: "), input.readRequired("Отдел: "));
        System.out.println("Сотрудник создан, ID = " + employee.getId());
    }

    private void updateEmployee() {
        long id = input.readId("ID сотрудника: ");
        employeeService.update(id, input.readRequired("Новое ФИО: "),
                input.readRequired("Новый email: "), input.readRequired("Новый отдел: "));
        System.out.println("Сотрудник обновлен.");
    }

    private void deleteEmployee() {
        long id = input.readId("ID сотрудника: ");
        if (input.confirm("Удалить сотрудника")) {
            employeeService.delete(id);
            System.out.println("Сотрудник удален.");
        }
    }

    private void createTrip() {
        System.out.println("Сначала выберите сотрудника:");
        printEmployees(employeeService.findAll());
        BusinessTrip trip = tripService.create(input.readId("ID сотрудника: "),
                input.readRequired("Место назначения: "), input.readRequired("Цель: "),
                input.readDate("Дата начала: "), input.readDate("Дата окончания: "),
                input.readMoney("Плановая стоимость: "));
        System.out.println("Поездка создана, ID = " + trip.getId());
    }

    private void updateTrip() {
        long id = input.readId("ID поездки: ");
        printEmployees(employeeService.findAll());
        tripService.update(id, input.readId("ID сотрудника: "),
                input.readRequired("Новое место: "), input.readRequired("Новая цель: "),
                input.readDate("Новая дата начала: "), input.readDate("Новая дата окончания: "),
                input.readMoney("Новая плановая стоимость: "));
        System.out.println("Поездка обновлена.");
    }

    private void changeTripStatus() {
        long id = input.readId("ID поездки: ");
        BusinessTrip current = tripService.findById(id);
        System.out.println("Текущий статус: " + current.getStatus());
        TripStatus target = input.readStatus("Новый статус: ");
        BigDecimal actualCost = target == TripStatus.COMPLETED
                ? input.readMoney("Фактическая стоимость: ") : null;
        tripService.changeStatus(id, target, actualCost);
        System.out.println("Статус обновлён.");
    }

    private void deleteTrip() {
        long id = input.readId("ID поездки: ");
        if (input.confirm("Удалить поездку")) {
            tripService.delete(id);
            System.out.println("Поездка удалена.");
        }
    }

    private void createTicket() {
        printTrips(tripService.findAll());
        TransportTicket ticket = ticketService.create(input.readId("ID поездки: "),
                input.readTransportType("Вид транспорта: "), input.readRequired("Перевозчик: "),
                input.readRequired("Номер билета: "), input.readRequired("Место отправления: "),
                input.readRequired("Место прибытия: "), input.readDateTime("Отправление (ГГГГ-ММ-ДД ЧЧ:ММ): "),
                input.readDateTime("Прибытие (ГГГГ-ММ-ДД ЧЧ:ММ): "), input.readMoney("Стоимость: "));
        System.out.println("Билет создан, ID = " + ticket.getId());
    }

    private void updateTicket() {
        long id = input.readId("ID билета: "); printTrips(tripService.findAll());
        ticketService.update(id, input.readId("ID поездки: "), input.readTransportType("Вид транспорта: "),
                input.readRequired("Перевозчик: "), input.readRequired("Номер билета: "),
                input.readRequired("Место отправления: "), input.readRequired("Место прибытия: "),
                input.readDateTime("Отправление: "), input.readDateTime("Прибытие: "), input.readMoney("Стоимость: "));
        System.out.println("Билет обновлён.");
    }

    private void deleteTicket() {
        long id = input.readId("ID билета: ");
        if (input.confirm("Удалить билет")) { ticketService.delete(id); System.out.println("Билет удалён."); }
    }

    private void createAccommodation() {
        printTrips(tripService.findAll());
        Accommodation value = accommodationService.create(input.readId("ID поездки: "),
                input.readRequired("Название гостиницы: "), input.readRequired("Адрес: "),
                input.readDate("Дата заселения: "), input.readDate("Дата выезда: "), input.readMoney("Стоимость: "));
        System.out.println("Проживание создано, ID = " + value.getId());
    }

    private void updateAccommodation() {
        long id = input.readId("ID проживания: "); printTrips(tripService.findAll());
        accommodationService.update(id, input.readId("ID поездки: "), input.readRequired("Название гостиницы: "),
                input.readRequired("Адрес: "), input.readDate("Дата заселения: "),
                input.readDate("Дата выезда: "), input.readMoney("Стоимость: "));
        System.out.println("Проживание обновлено.");
    }

    private void deleteAccommodation() {
        long id = input.readId("ID проживания: ");
        if (input.confirm("Удалить проживание")) { accommodationService.delete(id); System.out.println("Проживание удалено."); }
    }

    private void createExpense() {
        printTrips(tripService.findAll());
        TripExpense value = expenseService.create(input.readId("ID поездки: "),
                input.readExpenseCategory("Категория: "), input.readDate("Дата расхода: "),
                input.readRequired("Описание: "), input.readMoney("Сумма: "));
        System.out.println("Расход создан, ID = " + value.getId());
    }

    private void updateExpense() {
        long id = input.readId("ID расхода: "); printTrips(tripService.findAll());
        expenseService.update(id, input.readId("ID поездки: "), input.readExpenseCategory("Категория: "),
                input.readDate("Дата расхода: "), input.readRequired("Описание: "), input.readMoney("Сумма: "));
        System.out.println("Расход обновлён.");
    }

    private void deleteExpense() {
        long id = input.readId("ID расхода: ");
        if (input.confirm("Удалить расход")) { expenseService.delete(id); System.out.println("Расход удалён."); }
    }

    private void showStatistics() {
        TripStatistics statistics = tripService.statistics();
        System.out.println("\n========== СТАТИСТИКА ==========");
        System.out.println("Всего сотрудников: " + employeeService.findAll().size());
        System.out.println("Всего поездок: " + statistics.getTotal());
        System.out.println("Запланировано: " + statistics.getPlanned());
        System.out.println("Активных (согласованы/в процессе): " + statistics.getActive());
        System.out.println("Завершено: " + statistics.getCompleted());
        System.out.println("Отменено: " + statistics.getCancelled());
        System.out.println("Плановые затраты: " + statistics.getTotalPlannedCost());
        System.out.println("Фактические затраты: " + statistics.getTotalActualCost());
        System.out.printf("Средняя длительность: %.1f дн.%n", statistics.getAverageDurationDays());
    }

    private void exportData() {
        String defaultName = "exports/business-trips-" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".xlsx";
        String value = input.readLine("Путь к .xlsx [" + defaultName + "]: ");
        Path path = Paths.get(value.isEmpty() ? defaultName : value);
        try {
            Path result = exporter.export(employeeService.findAll(), tripService.findAll(),
                    ticketService.findAll(), accommodationService.findAll(), expenseService.findAll(), path);
            System.out.println("Данные экспортированы: " + result);
        } catch (IOException e) {
            throw new BusinessException("Ошибка экспорта: " + e.getMessage());
        }
    }

    private void showDatabaseTables() {
        System.out.println("\n========== ТАБЛИЦА employees ==========");
        printEmployees(employeeService.findAll());
        System.out.println("\n========== ТАБЛИЦА business_trips ==========");
        printTrips(tripService.findAll());
        System.out.println("\n========== ТАБЛИЦА transport_tickets ==========");
        printTickets(ticketService.findAll());
        System.out.println("\n========== ТАБЛИЦА accommodations ==========");
        printAccommodations(accommodationService.findAll());
        System.out.println("\n========== ТАБЛИЦА trip_expenses ==========");
        printExpenses(expenseService.findAll());
    }

    private static void printEmployees(List<Employee> employees) {
        if (employees.isEmpty()) { System.out.println("Сотрудники не найдены."); return; }
        System.out.println("ID | ФИО | EMAIL | ОТДЕЛ");
        employees.forEach(System.out::println);
    }

    private static void printTrips(List<BusinessTrip> trips) {
        if (trips.isEmpty()) { System.out.println("Поездки не найдены."); return; }
        System.out.println("ID | СОТРУДНИК | МЕСТО | ДАТЫ | СТАТУС | СТОИМОСТЬ");
        trips.forEach(System.out::println);
    }

    private static void printTickets(List<TransportTicket> tickets) {
        if (tickets.isEmpty()) { System.out.println("Билеты не найдены."); return; }
        System.out.println("ID | ПОЕЗДКА | ТРАНСПОРТ | ПЕРЕВОЗЧИК | НОМЕР | МАРШРУТ | ВРЕМЯ | СТОИМОСТЬ");
        tickets.forEach(System.out::println);
    }

    private static void printAccommodations(List<Accommodation> values) {
        if (values.isEmpty()) { System.out.println("Записи о проживании не найдены."); return; }
        System.out.println("ID | ПОЕЗДКА | ГОСТИНИЦА | АДРЕС | ДАТЫ | СТОИМОСТЬ");
        values.forEach(System.out::println);
    }

    private static void printExpenses(List<TripExpense> values) {
        if (values.isEmpty()) { System.out.println("Расходы не найдены."); return; }
        System.out.println("ID | ПОЕЗДКА | КАТЕГОРИЯ | ДАТА | ОПИСАНИЕ | СУММА");
        values.forEach(System.out::println);
    }

    private static void printTripDetails(BusinessTrip trip) {
        System.out.println(trip);
        System.out.println("Цель: " + trip.getPurpose());
        System.out.println("Создана: " + trip.getCreatedAt());
    }

    private static void printMainMenu() {
        System.out.println("\n============================================================");
        System.out.println("       СИСТЕМА ОФОРМЛЕНИЯ СЛУЖЕБНЫХ ПОЕЗДОК");
        System.out.println("============================================================");
        System.out.println("1. Сотрудники\n2. Служебные поездки\n3. Транспортные билеты\n4. Проживание\n5. Расходы");
        System.out.println("6. Поиск\n7. Фильтрация\n8. Статистика\n9. Экспорт в Excel");
        System.out.println("10. Вывести таблицы базы данных\n0. Выход");
    }

    private static void printError(String message) {
        System.out.println("\nОшибка: " + message);
    }
}
