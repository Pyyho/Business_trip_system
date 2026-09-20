package ru.pkc.travel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeServiceTest {
    private InMemoryRepositories.Employees employees;
    private InMemoryRepositories.Trips trips;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        employees = new InMemoryRepositories.Employees();
        trips = new InMemoryRepositories.Trips();
        service = new EmployeeService(employees, trips);
    }

    @Test
    void validatesAndNormalizesEmail() {
        Employee employee = service.create("Иван Соколов", " IVAN@EXAMPLE.COM ", "ИТ");
        assertEquals("ivan@example.com", employee.getEmail());
        assertThrows(BusinessException.class,
                () -> service.create("Пётр Орлов", "ivan@example.com", "ИТ"));
        assertThrows(BusinessException.class,
                () -> service.create("Пётр Орлов", "not-an-email", "ИТ"));
    }

    @Test
    void doesNotDeleteEmployeeWithTrips() {
        Employee employee = service.create("Иван Соколов", "ivan@example.com", "ИТ");
        trips.create(new BusinessTrip(employee, "Омск", "Аудит", LocalDate.now(),
                LocalDate.now().plusDays(1), BigDecimal.TEN));

        assertThrows(BusinessException.class, () -> service.delete(employee.getId()));
    }
}
