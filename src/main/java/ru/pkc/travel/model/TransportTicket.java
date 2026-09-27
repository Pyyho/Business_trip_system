package ru.pkc.travel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TransportTicket {
    private Long id;
    private BusinessTrip trip;
    private TransportType transportType;
    private String carrier;
    private String ticketNumber;
    private String departurePlace;
    private String arrivalPlace;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;
    private BigDecimal cost;

    public TransportTicket(Long id, BusinessTrip trip, TransportType transportType, String carrier,
                           String ticketNumber, String departurePlace, String arrivalPlace,
                           LocalDateTime departureAt, LocalDateTime arrivalAt, BigDecimal cost) {
        this.id = id;
        this.trip = trip;
        this.transportType = transportType;
        this.carrier = carrier;
        this.ticketNumber = ticketNumber;
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
        this.departureAt = departureAt;
        this.arrivalAt = arrivalAt;
        this.cost = cost;
    }

    public TransportTicket(BusinessTrip trip, TransportType transportType, String carrier,
                           String ticketNumber, String departurePlace, String arrivalPlace,
                           LocalDateTime departureAt, LocalDateTime arrivalAt, BigDecimal cost) {
        this(null, trip, transportType, carrier, ticketNumber, departurePlace, arrivalPlace,
                departureAt, arrivalAt, cost);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusinessTrip getTrip() { return trip; }
    public void setTrip(BusinessTrip trip) { this.trip = trip; }
    public TransportType getTransportType() { return transportType; }
    public void setTransportType(TransportType transportType) { this.transportType = transportType; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getDeparturePlace() { return departurePlace; }
    public void setDeparturePlace(String departurePlace) { this.departurePlace = departurePlace; }
    public String getArrivalPlace() { return arrivalPlace; }
    public void setArrivalPlace(String arrivalPlace) { this.arrivalPlace = arrivalPlace; }
    public LocalDateTime getDepartureAt() { return departureAt; }
    public void setDepartureAt(LocalDateTime departureAt) { this.departureAt = departureAt; }
    public LocalDateTime getArrivalAt() { return arrivalAt; }
    public void setArrivalAt(LocalDateTime arrivalAt) { this.arrivalAt = arrivalAt; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    @Override public String toString() {
        return String.format("%d | поездка %d | %s | %s | №%s | %s -> %s | %s — %s | %s",
                id, trip.getId(), transportType, carrier, ticketNumber, departurePlace, arrivalPlace,
                departureAt, arrivalAt, cost);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportTicket)) return false;
        TransportTicket that = (TransportTicket) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return id == null ? 0 : id.hashCode(); }
}
