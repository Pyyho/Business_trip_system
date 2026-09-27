package ru.pkc.travel.service;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.EntityNotFoundException;
import ru.pkc.travel.model.Accommodation;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.TripStatus;
import ru.pkc.travel.repository.AccommodationRepository;
import ru.pkc.travel.repository.BusinessTripRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AccommodationService {
    private final AccommodationRepository repository;
    private final BusinessTripRepository tripRepository;

    public AccommodationService(AccommodationRepository repository, BusinessTripRepository tripRepository) {
        this.repository = repository; this.tripRepository = tripRepository;
    }

    public Accommodation create(long tripId, String hotelName, String address, LocalDate checkIn,
                                LocalDate checkOut, BigDecimal cost) {
        BusinessTrip trip = requireTrip(tripId); requireEditableTrip(trip);
        Accommodation value = new Accommodation(trip, normalize(hotelName), normalize(address), checkIn, checkOut, cost);
        validate(value); return repository.create(value);
    }
    public List<Accommodation> findAll() { return repository.findAll(); }
    public List<Accommodation> findByTripId(long tripId) { requireTrip(tripId); return repository.findByTripId(tripId); }
    public Accommodation findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Проживание", id));
    }
    public Accommodation update(long id, long tripId, String hotelName, String address,
                                LocalDate checkIn, LocalDate checkOut, BigDecimal cost) {
        Accommodation value = findById(id); BusinessTrip trip = requireTrip(tripId); requireEditableTrip(trip);
        value.setTrip(trip); value.setHotelName(normalize(hotelName)); value.setAddress(normalize(address));
        value.setCheckInDate(checkIn); value.setCheckOutDate(checkOut); value.setCost(cost);
        validate(value); repository.update(value); return value;
    }
    public void delete(long id) { findById(id); repository.deleteById(id); }

    private BusinessTrip requireTrip(long id) {
        return tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Служебная поездка", id));
    }
    private static void requireEditableTrip(BusinessTrip trip) {
        if (trip.getStatus() != TripStatus.PLANNED && trip.getStatus() != TripStatus.APPROVED)
            throw new BusinessException("Проживание можно оформлять только до начала поездки.");
    }
    private static void validate(Accommodation a) {
        if (a.getHotelName().length() < 2 || a.getAddress().length() < 3)
            throw new BusinessException("Название и адрес места проживания обязательны.");
        if (a.getCheckInDate() == null || a.getCheckOutDate() == null || a.getCheckOutDate().isBefore(a.getCheckInDate()))
            throw new BusinessException("Выезд из гостиницы не может быть раньше заселения.");
        if (a.getCheckInDate().isBefore(a.getTrip().getStartDate()) || a.getCheckOutDate().isAfter(a.getTrip().getEndDate()))
            throw new BusinessException("Период проживания должен входить в период поездки.");
        if (a.getCost() == null || a.getCost().compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException("Стоимость проживания не может быть отрицательной.");
    }
    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
