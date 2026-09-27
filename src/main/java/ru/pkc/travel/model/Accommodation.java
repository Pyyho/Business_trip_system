package ru.pkc.travel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Accommodation {
    private Long id;
    private BusinessTrip trip;
    private String hotelName;
    private String address;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal cost;

    public Accommodation(Long id, BusinessTrip trip, String hotelName, String address,
                         LocalDate checkInDate, LocalDate checkOutDate, BigDecimal cost) {
        this.id = id;
        this.trip = trip;
        this.hotelName = hotelName;
        this.address = address;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.cost = cost;
    }

    public Accommodation(BusinessTrip trip, String hotelName, String address,
                         LocalDate checkInDate, LocalDate checkOutDate, BigDecimal cost) {
        this(null, trip, hotelName, address, checkInDate, checkOutDate, cost);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusinessTrip getTrip() { return trip; }
    public void setTrip(BusinessTrip trip) { this.trip = trip; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    @Override public String toString() {
        return String.format("%d | поездка %d | %s | %s | %s — %s | %s",
                id, trip.getId(), hotelName, address, checkInDate, checkOutDate, cost);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Accommodation)) return false;
        Accommodation that = (Accommodation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return id == null ? 0 : id.hashCode(); }
}
