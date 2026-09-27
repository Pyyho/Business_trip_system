package ru.pkc.travel.service;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A dependency-free executable check for environments where the JUnit runner is unavailable. */
public final class ServiceSmokeCheck {
    private ServiceSmokeCheck() { }

    public static void main(String[] args) {
        InMemoryRepositories.Employees employees = new InMemoryRepositories.Employees();
        InMemoryRepositories.Trips trips = new InMemoryRepositories.Trips();
        EmployeeService employeeService = new EmployeeService(employees, trips);
        BusinessTripService tripService = new BusinessTripService(trips, employees);
        TransportTicketService ticketService = new TransportTicketService(new InMemoryRepositories.Tickets(), trips);
        AccommodationService accommodationService = new AccommodationService(new InMemoryRepositories.Accommodations(), trips);
        TripExpenseService expenseService = new TripExpenseService(new InMemoryRepositories.Expenses(), trips);

        Employee employee = employeeService.create("Анна Петрова", "ANNA@example.com", "Продажи");
        require("anna@example.com".equals(employee.getEmail()), "Email was not normalized");

        BusinessTrip trip = tripService.create(employee.getId(), "Казань", "Переговоры",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), new BigDecimal("10000"));
        ticketService.create(trip.getId(), TransportType.TRAIN, "РЖД", "SMOKE-1", "Москва", "Казань",
                LocalDateTime.of(2026, 10, 1, 8, 0), LocalDateTime.of(2026, 10, 1, 16, 0), new BigDecimal("3000"));
        accommodationService.create(trip.getId(), "Отель", "ул. Ленина, 1", LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 3), new BigDecimal("5000"));
        tripService.changeStatus(trip.getId(), TripStatus.APPROVED, null);
        expenseService.create(trip.getId(), ExpenseCategory.MEALS, LocalDate.of(2026, 10, 2),
                "Обед", new BigDecimal("800"));
        tripService.changeStatus(trip.getId(), TripStatus.IN_PROGRESS, null);
        tripService.changeStatus(trip.getId(), TripStatus.COMPLETED, new BigDecimal("9500"));

        require(trip.getStatus() == TripStatus.COMPLETED, "Status transition failed");
        require(tripService.searchByDestination("каз").size() == 1, "Search failed");
        require(tripService.filterByStatus(TripStatus.COMPLETED).size() == 1, "Filter failed");
        require(ticketService.findByTripId(trip.getId()).size() == 1, "Ticket CRUD failed");
        require(accommodationService.findByTripId(trip.getId()).size() == 1, "Accommodation CRUD failed");
        require(expenseService.findByTripId(trip.getId()).size() == 1, "Expense CRUD failed");

        TripStatistics statistics = tripService.statistics();
        require(statistics.getTotal() == 1, "Statistics total is wrong");
        require(statistics.getCompleted() == 1, "Statistics completed count is wrong");
        require(new BigDecimal("9500").equals(statistics.getTotalActualCost()), "Actual cost total is wrong");

        boolean rejected = false;
        try {
            tripService.changeStatus(trip.getId(), TripStatus.PLANNED, null);
        } catch (BusinessException expected) {
            rejected = true;
        }
        require(rejected, "Forbidden status transition was accepted");
        System.out.println("SERVICE_SMOKE_CHECK_OK");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
