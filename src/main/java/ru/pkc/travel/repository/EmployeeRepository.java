package ru.pkc.travel.repository;

import ru.pkc.travel.model.Employee;

public interface EmployeeRepository extends CrudRepository<Employee> {
    boolean existsByEmail(String email, Long excludedId);
}
