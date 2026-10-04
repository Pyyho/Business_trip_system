package ru.pkc.travel.service;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.EntityNotFoundException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.TripStatistics;
import ru.pkc.travel.model.TripStatus;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BusinessTripService {
    private final BusinessTripRepository tripRepository;
    private final EmployeeRepository employeeRepository;

    public BusinessTripService(BusinessTripRepository tripRepository, EmployeeRepository employeeRepository) {
        this.tripRepository = tripRepository;
        this.employeeRepository = employeeRepository;
    }

    public BusinessTrip create(long employeeId, String destination, String purpose,
                               LocalDate startDate, LocalDate endDate, BigDecimal plannedCost) {
        Employee employee = requireEmployee(employeeId);
        BusinessTrip trip = new BusinessTrip(employee, normalize(destination), normalize(purpose),
                startDate, endDate, plannedCost);
        validate(trip);
        return tripRepository.create(trip);
    }

    public List<BusinessTrip> findAll() { return tripRepository.findAll(); }

    public BusinessTrip findById(long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Служебная поездка", id));
    }

    public BusinessTrip update(long id, long employeeId, String destination, String purpose,
                               LocalDate startDate, LocalDate endDate, BigDecimal plannedCost) {
        BusinessTrip trip = findById(id);
        if (trip.getStatus() != TripStatus.PLANNED) {
            throw new BusinessException("Изменять детали можно только у запланированной поездки.");
        }
        trip.setEmployee(requireEmployee(employeeId));
        trip.setDestination(normalize(destination));
        trip.setPurpose(normalize(purpose));
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setPlannedCost(plannedCost);
        validate(trip);
        tripRepository.update(trip);
        return trip;
    }

    public BusinessTrip changeStatus(long id, TripStatus target, BigDecimal actualCost) {
        BusinessTrip trip = findById(id);
        if (target == null || !trip.getStatus().canTransitionTo(target)) {
            throw new BusinessException("Запрещенный переход: " + trip.getStatus() + " -> " + target + ".");
        }
        if (target == TripStatus.COMPLETED) {
            if (actualCost == null || actualCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Для завершенной поездки нужна неотрицательная фактическая стоимость.");
            }
            trip.setActualCost(actualCost);
        } else if (actualCost != null) {
            throw new BusinessException("Фактическая стоимость указывается только при завершении.");
        }
        trip.setStatus(target);
        tripRepository.update(trip);
        return trip;
    }

    public void delete(long id) {
        BusinessTrip trip = findById(id);
        if (trip.getStatus() == TripStatus.IN_PROGRESS || trip.getStatus() == TripStatus.COMPLETED) {
            throw new BusinessException("Нельзя удалить начатую или завершенную поездку.");
        }
        tripRepository.deleteById(id);
    }

    public List<BusinessTrip> searchByDestination(String query) {
        requireQuery(query);
        return tripRepository.searchByDestination(query.trim());
    }

    public List<BusinessTrip> searchByEmployee(String query) {
        requireQuery(query);
        return tripRepository.searchByEmployee(query.trim());
    }

    public List<BusinessTrip> filterByStatus(TripStatus status) {
        if (status == null) throw new BusinessException("Статус не задан.");
        return tripRepository.findByStatus(status);
    }

    public List<BusinessTrip> filterByDates(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BusinessException("Конец диапазона дат не может быть раньше начала.");
        }
        return tripRepository.findByDateRange(from, to);
    }

    public List<BusinessTrip> sortByStartDate() {
        return tripRepository.findAll().stream()
                .sorted(Comparator.comparing(BusinessTrip::getStartDate)
                        .thenComparing(BusinessTrip::getId))
                .collect(Collectors.toList());
    }

    public List<BusinessTrip> sortByPlannedCostDescending() {
        return tripRepository.findAll().stream()
                .sorted(Comparator.comparing(BusinessTrip::getPlannedCost).reversed()
                        .thenComparing(BusinessTrip::getId))
                .collect(Collectors.toList());
    }

    public List<BusinessTrip> sortByStartDateDescending() {
        return tripRepository.findAll().stream()
                .sorted(Comparator.comparing(BusinessTrip::getStartDate).reversed()
                        .thenComparing(BusinessTrip::getId))
                .collect(Collectors.toList());
    }

    public List<BusinessTrip> sortByPlannedCostAscending() {
        return tripRepository.findAll().stream()
                .sorted(Comparator.comparing(BusinessTrip::getPlannedCost)
                        .thenComparing(BusinessTrip::getId))
                .collect(Collectors.toList());
    }

    public TripStatistics statistics() {
        List<BusinessTrip> trips = tripRepository.findAll();
        long planned = count(trips, TripStatus.PLANNED);
        long active = trips.stream().filter(t -> t.getStatus() == TripStatus.APPROVED
                || t.getStatus() == TripStatus.IN_PROGRESS).count();
        long completed = count(trips, TripStatus.COMPLETED);
        long cancelled = count(trips, TripStatus.CANCELLED);
        BigDecimal plannedCost = trips.stream().map(BusinessTrip::getPlannedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualCost = trips.stream().map(BusinessTrip::getActualCost)
                .filter(value -> value != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        double averageDays = trips.stream().mapToLong(t ->
                ChronoUnit.DAYS.between(t.getStartDate(), t.getEndDate()) + 1).average().orElse(0.0);
        return new TripStatistics(trips.size(), planned, active, completed, cancelled,
                plannedCost, actualCost, averageDays);
    }

    private Employee requireEmployee(long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник", employeeId));
    }

    private static long count(List<BusinessTrip> trips, TripStatus status) {
        return trips.stream().filter(t -> t.getStatus() == status).count();
    }

    private static void requireQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new BusinessException("Поисковая строка не может быть пустой.");
        }
    }

    private static void validate(BusinessTrip trip) {
        if (trip.getEmployee() == null || trip.getEmployee().getId() == null) {
            throw new BusinessException("Для поездки необходим существующий сотрудник.");
        }
        if (trip.getDestination().length() < 2) {
            throw new BusinessException("Место назначения должно содержать не менее 2 символов.");
        }
        if (trip.getPurpose().length() < 3) {
            throw new BusinessException("Цель поездки должна содержать не менее 3 символов.");
        }
        if (trip.getStartDate() == null || trip.getEndDate() == null || trip.getEndDate().isBefore(trip.getStartDate())) {
            throw new BusinessException("Дата окончания не может быть раньше даты начала.");
        }
        if (trip.getPlannedCost() == null || trip.getPlannedCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Плановая стоимость должна быть неотрицательной.");
        }
    }

    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
