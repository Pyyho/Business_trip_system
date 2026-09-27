package ru.pkc.travel.service;

import ru.pkc.travel.model.*;
import ru.pkc.travel.repository.AccommodationRepository;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.EmployeeRepository;
import ru.pkc.travel.repository.TransportTicketRepository;
import ru.pkc.travel.repository.TripExpenseRepository;

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

    static final class Tickets implements TransportTicketRepository {
        private final Map<Long, TransportTicket> data = new LinkedHashMap<>();
        private long nextId = 1;
        @Override public TransportTicket create(TransportTicket entity) { entity.setId(nextId++); data.put(entity.getId(), entity); return entity; }
        @Override public List<TransportTicket> findAll() { return new ArrayList<>(data.values()); }
        @Override public Optional<TransportTicket> findById(long id) { return Optional.ofNullable(data.get(id)); }
        @Override public void update(TransportTicket entity) { data.put(entity.getId(), entity); }
        @Override public void deleteById(long id) { data.remove(id); }
        @Override public List<TransportTicket> findByTripId(long tripId) { return data.values().stream().filter(x -> x.getTrip().getId() == tripId).collect(Collectors.toList()); }
        @Override public boolean existsByTicketNumber(String number, Long excludedId) {
            return data.values().stream().anyMatch(x -> x.getTicketNumber().equalsIgnoreCase(number)
                    && (excludedId == null || !excludedId.equals(x.getId())));
        }
    }

    static final class Accommodations implements AccommodationRepository {
        private final Map<Long, Accommodation> data = new LinkedHashMap<>();
        private long nextId = 1;
        @Override public Accommodation create(Accommodation entity) { entity.setId(nextId++); data.put(entity.getId(), entity); return entity; }
        @Override public List<Accommodation> findAll() { return new ArrayList<>(data.values()); }
        @Override public Optional<Accommodation> findById(long id) { return Optional.ofNullable(data.get(id)); }
        @Override public void update(Accommodation entity) { data.put(entity.getId(), entity); }
        @Override public void deleteById(long id) { data.remove(id); }
        @Override public List<Accommodation> findByTripId(long tripId) { return data.values().stream().filter(x -> x.getTrip().getId() == tripId).collect(Collectors.toList()); }
    }

    static final class Expenses implements TripExpenseRepository {
        private final Map<Long, TripExpense> data = new LinkedHashMap<>();
        private long nextId = 1;
        @Override public TripExpense create(TripExpense entity) { entity.setId(nextId++); data.put(entity.getId(), entity); return entity; }
        @Override public List<TripExpense> findAll() { return new ArrayList<>(data.values()); }
        @Override public Optional<TripExpense> findById(long id) { return Optional.ofNullable(data.get(id)); }
        @Override public void update(TripExpense entity) { data.put(entity.getId(), entity); }
        @Override public void deleteById(long id) { data.remove(id); }
        @Override public List<TripExpense> findByTripId(long tripId) { return data.values().stream().filter(x -> x.getTrip().getId() == tripId).collect(Collectors.toList()); }
    }
}
