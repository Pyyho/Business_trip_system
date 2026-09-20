package ru.pkc.travel.service;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.EntityNotFoundException;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.EmployeeRepository;

import java.util.List;
import java.util.regex.Pattern;

public class EmployeeService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final EmployeeRepository employeeRepository;
    private final BusinessTripRepository tripRepository;

    public EmployeeService(EmployeeRepository employeeRepository, BusinessTripRepository tripRepository) {
        this.employeeRepository = employeeRepository;
        this.tripRepository = tripRepository;
    }

    public Employee create(String fullName, String email, String department) {
        Employee employee = new Employee(normalize(fullName), normalize(email).toLowerCase(), normalize(department));
        validate(employee);
        ensureUniqueEmail(employee.getEmail(), null);
        return employeeRepository.create(employee);
    }

    public List<Employee> findAll() { return employeeRepository.findAll(); }

    public Employee findById(long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник", id));
    }

    public Employee update(long id, String fullName, String email, String department) {
        Employee employee = findById(id);
        employee.setFullName(normalize(fullName));
        employee.setEmail(normalize(email).toLowerCase());
        employee.setDepartment(normalize(department));
        validate(employee);
        ensureUniqueEmail(employee.getEmail(), id);
        employeeRepository.update(employee);
        return employee;
    }

    public void delete(long id) {
        findById(id);
        if (tripRepository.existsByEmployeeId(id)) {
            throw new BusinessException("Нельзя удалить сотрудника: с ним связаны служебные поездки.");
        }
        employeeRepository.deleteById(id);
    }

    private void validate(Employee employee) {
        if (employee.getFullName().length() < 3) {
            throw new BusinessException("ФИО должно содержать не менее 3 символов.");
        }
        if (!EMAIL.matcher(employee.getEmail()).matches()) {
            throw new BusinessException("Некорректный email сотрудника.");
        }
        if (employee.getDepartment().length() < 2) {
            throw new BusinessException("Название отдела должно содержать не менее 2 символов.");
        }
    }

    private void ensureUniqueEmail(String email, Long excludedId) {
        if (employeeRepository.existsByEmail(email, excludedId)) {
            throw new BusinessException("Сотрудник с таким email уже существует.");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
