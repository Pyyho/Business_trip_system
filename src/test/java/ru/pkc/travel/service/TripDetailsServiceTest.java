package ru.pkc.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.ExpenseCategory;
import ru.pkc.travel.model.TransportType;
import ru.pkc.travel.model.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TripDetailsServiceTest {
    private InMemoryRepositories.Trips trips;
    private BusinessTrip trip;
    private TransportTicketService ticketService;
    private AccommodationService accommodationService;
    private TripExpenseService expenseService;

    @BeforeEach
    void setUp() {
        InMemoryRepositories.Employees employees = new InMemoryRepositories.Employees();
        trips = new InMemoryRepositories.Trips();
        Employee employee = employees.create(new Employee("Анна Петрова", "anna@example.com", "Продажи"));
        trip = trips.create(new BusinessTrip(employee, "Казань", "Переговоры",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), new BigDecimal("50000")));
        ticketService = new TransportTicketService(new InMemoryRepositories.Tickets(), trips);
        accommodationService = new AccommodationService(new InMemoryRepositories.Accommodations(), trips);
        expenseService = new TripExpenseService(new InMemoryRepositories.Expenses(), trips);
    }

    @Test
    void createsTicketAndRejectsDuplicateNumber() {
        ticketService.create(trip.getId(), TransportType.TRAIN, "РЖД", "A-123", "Москва", "Казань",
                LocalDateTime.of(2026, 10, 1, 8, 0), LocalDateTime.of(2026, 10, 1, 16, 0), new BigDecimal("5000"));
        assertEquals(1, ticketService.findByTripId(trip.getId()).size());
        assertThrows(BusinessException.class, () -> ticketService.create(trip.getId(), TransportType.TRAIN,
                "РЖД", "A-123", "Москва", "Казань", LocalDateTime.of(2026, 10, 2, 8, 0),
                LocalDateTime.of(2026, 10, 2, 16, 0), BigDecimal.TEN));
    }

    @Test
    void accommodationMustFitTripDates() {
        accommodationService.create(trip.getId(), "Отель", "ул. Ленина, 1", LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5), new BigDecimal("20000"));
        assertEquals(1, accommodationService.findAll().size());
        assertThrows(BusinessException.class, () -> accommodationService.create(trip.getId(), "Отель", "Адрес",
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 10, 5), BigDecimal.TEN));
    }

    @Test
    void expenseRequiresApprovedTripAndValidDate() {
        assertThrows(BusinessException.class, () -> expenseService.create(trip.getId(), ExpenseCategory.MEALS,
                LocalDate.of(2026, 10, 2), "Обед", new BigDecimal("800")));
        trip.setStatus(TripStatus.APPROVED); trips.update(trip);
        expenseService.create(trip.getId(), ExpenseCategory.MEALS, LocalDate.of(2026, 10, 2),
                "Обед", new BigDecimal("800"));
        assertEquals(1, expenseService.findAll().size());
    }
}
