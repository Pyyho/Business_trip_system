package ru.pkc.travel.repository;

import ru.pkc.travel.model.TripExpense;
import java.util.List;

public interface TripExpenseRepository extends CrudRepository<TripExpense> {
    List<TripExpense> findByTripId(long tripId);
}
