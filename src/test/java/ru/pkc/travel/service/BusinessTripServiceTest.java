package ru.pkc.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.EntityNotFoundException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.TripStatistics;
import ru.pkc.travel.model.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessTripServiceTest {
    private InMemoryRepositories.Employees employees;
    private InMemoryRepositories.Trips trips;
    private BusinessTripService service;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employees = new InMemoryRepositories.Employees();
        trips = new InMemoryRepositories.Trips();
        employee = employees.create(new Employee("Анна Петрова", "anna@example.com", "Продажи"));
        service = new BusinessTripService(trips, employees);
    }

    @Test
    void createsTripOnlyForExistingEmployee() {
        BusinessTrip trip = service.create(employee.getId(), "Казань", "Переговоры",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), new BigDecimal("10000"));

        assertEquals(TripStatus.PLANNED, trip.getStatus());
        assertThrows(EntityNotFoundException.class, () -> service.create(999, "Омск", "Аудит",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), BigDecimal.ONE));
    }

    @Test
    void rejectsInvalidDatesAndNegativeCost() {
        assertThrows(BusinessException.class, () -> service.create(employee.getId(), "Омск", "Аудит",
                LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 1), BigDecimal.ONE));
        assertThrows(BusinessException.class, () -> service.create(employee.getId(), "Омск", "Аудит",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), new BigDecimal("-1")));
    }

    @Test
    void enforcesStatusTransitionsAndActualCost() {
        BusinessTrip trip = createTrip("10000");

        assertThrows(BusinessException.class,
                () -> service.changeStatus(trip.getId(), TripStatus.COMPLETED, BigDecimal.TEN));
        service.changeStatus(trip.getId(), TripStatus.APPROVED, null);
        service.changeStatus(trip.getId(), TripStatus.IN_PROGRESS, null);
        assertThrows(BusinessException.class,
                () -> service.changeStatus(trip.getId(), TripStatus.COMPLETED, null));
        service.changeStatus(trip.getId(), TripStatus.COMPLETED, new BigDecimal("9500"));

        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertEquals(new BigDecimal("9500"), trip.getActualCost());
    }

    @Test
    void calculatesStatisticsAndSortsUsingCollections() {
        BusinessTrip first = createTrip("10000");
        service.create(employee.getId(), "Омск", "Аудит", LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1), new BigDecimal("20000"));
        service.changeStatus(first.getId(), TripStatus.APPROVED, null);
        service.changeStatus(first.getId(), TripStatus.IN_PROGRESS, null);
        service.changeStatus(first.getId(), TripStatus.COMPLETED, new BigDecimal("9000"));

        TripStatistics statistics = service.statistics();
        assertEquals(2, statistics.getTotal());
        assertEquals(1, statistics.getCompleted());
        assertEquals(new BigDecimal("30000"), statistics.getTotalPlannedCost());
        assertEquals(new BigDecimal("9000"), statistics.getTotalActualCost());
        assertEquals(new BigDecimal("20000"), service.sortByPlannedCostDescending().get(0).getPlannedCost());
        assertEquals(LocalDate.of(2026, 11, 1), service.sortByStartDateDescending().get(0).getStartDate());
        assertEquals(new BigDecimal("10000"), service.sortByPlannedCostAscending().get(0).getPlannedCost());
    }

    private BusinessTrip createTrip(String cost) {
        return service.create(employee.getId(), "Казань", "Переговоры",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), new BigDecimal(cost));
    }
}
