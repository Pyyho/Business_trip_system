package ru.pkc.travel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class BusinessTrip {
    private Long id;
    private Employee employee;
    private String destination;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private TripStatus status;
    private BigDecimal plannedCost;
    private BigDecimal actualCost;
    private LocalDateTime createdAt;

    public BusinessTrip(Long id, Employee employee, String destination, String purpose,
                        LocalDate startDate, LocalDate endDate, TripStatus status,
                        BigDecimal plannedCost, BigDecimal actualCost, LocalDateTime createdAt) {
        this.id = id;
        this.employee = employee;
        this.destination = destination;
        this.purpose = purpose;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.plannedCost = plannedCost;
        this.actualCost = actualCost;
        this.createdAt = createdAt;
    }

    public BusinessTrip(Employee employee, String destination, String purpose, LocalDate startDate,
                        LocalDate endDate, BigDecimal plannedCost) {
        this(null, employee, destination, purpose, startDate, endDate, TripStatus.PLANNED,
                plannedCost, null, null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    public BigDecimal getPlannedCost() { return plannedCost; }
    public void setPlannedCost(BigDecimal plannedCost) { this.plannedCost = plannedCost; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %s — %s | %s | план: %s | факт: %s",
                id, employee == null ? "?" : employee.getFullName(), destination, startDate, endDate,
                status, plannedCost, actualCost == null ? "—" : actualCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessTrip)) return false;
        BusinessTrip that = (BusinessTrip) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return id == null ? 0 : id.hashCode(); }
}
