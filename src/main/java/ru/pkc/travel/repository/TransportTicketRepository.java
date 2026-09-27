package ru.pkc.travel.repository;

import ru.pkc.travel.model.TransportTicket;
import java.util.List;

public interface TransportTicketRepository extends CrudRepository<TransportTicket> {
    List<TransportTicket> findByTripId(long tripId);
    boolean existsByTicketNumber(String ticketNumber, Long excludedId);
}
