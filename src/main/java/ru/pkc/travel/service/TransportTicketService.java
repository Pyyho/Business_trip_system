package ru.pkc.travel.service;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.EntityNotFoundException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.TransportTicket;
import ru.pkc.travel.model.TransportType;
import ru.pkc.travel.model.TripStatus;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.repository.TransportTicketRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransportTicketService {
    private final TransportTicketRepository repository;
    private final BusinessTripRepository tripRepository;

    public TransportTicketService(TransportTicketRepository repository, BusinessTripRepository tripRepository) {
        this.repository = repository;
        this.tripRepository = tripRepository;
    }

    public TransportTicket create(long tripId, TransportType type, String carrier, String ticketNumber,
                                  String departurePlace, String arrivalPlace, LocalDateTime departureAt,
                                  LocalDateTime arrivalAt, BigDecimal cost) {
        BusinessTrip trip = requireTrip(tripId);
        requireEditableTrip(trip);
        TransportTicket ticket = new TransportTicket(trip, type, normalize(carrier), normalize(ticketNumber),
                normalize(departurePlace), normalize(arrivalPlace), departureAt, arrivalAt, cost);
        validate(ticket);
        ensureUniqueNumber(ticket.getTicketNumber(), null);
        return repository.create(ticket);
    }

    public List<TransportTicket> findAll() { return repository.findAll(); }
    public List<TransportTicket> findByTripId(long tripId) { requireTrip(tripId); return repository.findByTripId(tripId); }
    public TransportTicket findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Транспортный билет", id));
    }

    public TransportTicket update(long id, long tripId, TransportType type, String carrier,
                                  String ticketNumber, String departurePlace, String arrivalPlace,
                                  LocalDateTime departureAt, LocalDateTime arrivalAt, BigDecimal cost) {
        TransportTicket ticket = findById(id);
        BusinessTrip trip = requireTrip(tripId);
        requireEditableTrip(trip);
        ticket.setTrip(trip); ticket.setTransportType(type); ticket.setCarrier(normalize(carrier));
        ticket.setTicketNumber(normalize(ticketNumber)); ticket.setDeparturePlace(normalize(departurePlace));
        ticket.setArrivalPlace(normalize(arrivalPlace)); ticket.setDepartureAt(departureAt);
        ticket.setArrivalAt(arrivalAt); ticket.setCost(cost);
        validate(ticket); ensureUniqueNumber(ticket.getTicketNumber(), id); repository.update(ticket);
        return ticket;
    }

    public void delete(long id) { findById(id); repository.deleteById(id); }

    private BusinessTrip requireTrip(long id) {
        return tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Служебная поездка", id));
    }
    private static void requireEditableTrip(BusinessTrip trip) {
        if (trip.getStatus() != TripStatus.PLANNED && trip.getStatus() != TripStatus.APPROVED) {
            throw new BusinessException("Билеты можно оформлять только до начала поездки.");
        }
    }
    private void ensureUniqueNumber(String number, Long excludedId) {
        if (repository.existsByTicketNumber(number, excludedId)) {
            throw new BusinessException("Билет с таким номером уже существует.");
        }
    }
    private static void validate(TransportTicket t) {
        if (t.getTransportType() == null) throw new BusinessException("Вид транспорта обязателен.");
        if (t.getCarrier().length() < 2 || t.getTicketNumber().isEmpty())
            throw new BusinessException("Перевозчик и номер билета обязательны.");
        if (t.getDeparturePlace().length() < 2 || t.getArrivalPlace().length() < 2)
            throw new BusinessException("Пункты отправления и прибытия обязательны.");
        if (t.getDepartureAt() == null || t.getArrivalAt() == null || t.getArrivalAt().isBefore(t.getDepartureAt()))
            throw new BusinessException("Прибытие не может быть раньше отправления.");
        if (t.getDepartureAt().toLocalDate().isBefore(t.getTrip().getStartDate())
                || t.getArrivalAt().toLocalDate().isAfter(t.getTrip().getEndDate()))
            throw new BusinessException("Даты билета должны входить в период поездки.");
        if (t.getCost() == null || t.getCost().compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException("Стоимость билета не может быть отрицательной.");
    }
    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
