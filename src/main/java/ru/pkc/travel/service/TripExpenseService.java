package ru.pkc.travel.service;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.EntityNotFoundException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.ExpenseCategory;
import ru.pkc.travel.model.TripExpense;
import ru.pkc.travel.model.TripStatus;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.TripExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TripExpenseService {
    private final TripExpenseRepository repository;
    private final BusinessTripRepository tripRepository;

    public TripExpenseService(TripExpenseRepository repository, BusinessTripRepository tripRepository) {
        this.repository = repository; this.tripRepository = tripRepository;
    }

    public TripExpense create(long tripId, ExpenseCategory category, LocalDate date,
                              String description, BigDecimal amount) {
        BusinessTrip trip = requireTrip(tripId); requireExpenseAllowed(trip);
        TripExpense value = new TripExpense(trip, category, date, normalize(description), amount);
        validate(value); return repository.create(value);
    }
    public List<TripExpense> findAll() { return repository.findAll(); }
    public List<TripExpense> findByTripId(long tripId) { requireTrip(tripId); return repository.findByTripId(tripId); }
    public TripExpense findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Расход", id));
    }
    public TripExpense update(long id, long tripId, ExpenseCategory category, LocalDate date,
                              String description, BigDecimal amount) {
        TripExpense value = findById(id); BusinessTrip trip = requireTrip(tripId); requireExpenseAllowed(trip);
        value.setTrip(trip); value.setCategory(category); value.setExpenseDate(date);
        value.setDescription(normalize(description)); value.setAmount(amount);
        validate(value); repository.update(value); return value;
    }
    public void delete(long id) { findById(id); repository.deleteById(id); }

    private BusinessTrip requireTrip(long id) {
        return tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Служебная поездка", id));
    }
    private static void requireExpenseAllowed(BusinessTrip trip) {
        if (trip.getStatus() == TripStatus.PLANNED || trip.getStatus() == TripStatus.CANCELLED)
            throw new BusinessException("Расходы можно вносить только для согласованной, начатой или завершённой поездки.");
    }
    private static void validate(TripExpense x) {
        if (x.getCategory() == null) throw new BusinessException("Категория расхода обязательна.");
        if (x.getDescription().length() < 3) throw new BusinessException("Описание расхода должно содержать не менее 3 символов.");
        if (x.getExpenseDate() == null || x.getExpenseDate().isBefore(x.getTrip().getStartDate())
                || x.getExpenseDate().isAfter(x.getTrip().getEndDate()))
            throw new BusinessException("Дата расхода должна входить в период поездки.");
        if (x.getAmount() == null || x.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("Сумма расхода должна быть больше нуля.");
    }
    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
