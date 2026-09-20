package ru.pkc.travel.repository;

import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.TripStatus;

import java.time.LocalDate;
import java.util.List;

public interface BusinessTripRepository extends CrudRepository<BusinessTrip> {
    List<BusinessTrip> searchByDestination(String query);
    List<BusinessTrip> searchByEmployee(String query);
    List<BusinessTrip> findByStatus(TripStatus status);
    List<BusinessTrip> findByDateRange(LocalDate from, LocalDate to);
    boolean existsByEmployeeId(long employeeId);
}
