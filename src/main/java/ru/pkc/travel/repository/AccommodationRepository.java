package ru.pkc.travel.repository;

import ru.pkc.travel.model.Accommodation;
import java.util.List;

public interface AccommodationRepository extends CrudRepository<Accommodation> {
    List<Accommodation> findByTripId(long tripId);
}
