package ru.pkc.travel.service;

import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.TripStatus;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class InMemoryRepositories {
    private InMemoryRepositories() { }

    static final class Employees implements EmployeeRepository {
        private final Map<Long, Employee> data = new LinkedHashMap<>();
        private long nextId = 1;

        @Override public Employee create(Employee entity) {
            entity.setId(nextId++);
            data.put(entity.getId(), entity);
            return entity;
        }
        @Override public List<Employee> findAll() { return new ArrayList<>(data.values()); }
        @Override public Optional<Employee> findById(long id) { return Optional.ofNullable(data.get(id)); }
        @Override public void update(Employee entity) { data.put(entity.getId(), entity); }
        @Override public void deleteById(long id) { data.remove(id); }
        @Override public boolean existsByEmail(String email, Long excludedId) {
            return data.values().stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(email)
                    && (excludedId == null || !excludedId.equals(e.getId())));
        }
    }

    static final class Trips implements BusinessTripRepository {
        private final Map<Long, BusinessTrip> data = new LinkedHashMap<>();
        private long nextId = 1;

        @Override public BusinessTrip create(BusinessTrip entity) {
            entity.setId(nextId++);
            data.put(entity.getId(), entity);
            return entity;
        }
        @Override public List<BusinessTrip> findAll() { return new ArrayList<>(data.values()); }
        @Override public Optional<BusinessTrip> findById(long id) { return Optional.ofNullable(data.get(id)); }
        @Override public void update(BusinessTrip entity) { data.put(entity.getId(), entity); }
        @Override public void deleteById(long id) { data.remove(id); }
        @Override public List<BusinessTrip> searchByDestination(String query) {
            return data.values().stream().filter(t -> t.getDestination().toLowerCase()
                    .contains(query.toLowerCase())).collect(Collectors.toList());
        }
        @Override public List<BusinessTrip> searchByEmployee(String query) {
            return data.values().stream().filter(t -> t.getEmployee().getFullName().toLowerCase()
                    .contains(query.toLowerCase())).collect(Collectors.toList());
        }
        @Override public List<BusinessTrip> findByStatus(TripStatus status) {
            return data.values().stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        @Override public List<BusinessTrip> findByDateRange(LocalDate from, LocalDate to) {
            return data.values().stream().filter(t -> !t.getStartDate().isAfter(to)
                    && !t.getEndDate().isBefore(from)).collect(Collectors.toList());
        }
        @Override public boolean existsByEmployeeId(long employeeId) {
            return data.values().stream().anyMatch(t -> t.getEmployee().getId() == employeeId);
        }
    }
}
