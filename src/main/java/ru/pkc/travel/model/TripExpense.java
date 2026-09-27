package ru.pkc.travel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class TripExpense {
    private Long id;
    private BusinessTrip trip;
    private ExpenseCategory category;
    private LocalDate expenseDate;
    private String description;
    private BigDecimal amount;

    public TripExpense(Long id, BusinessTrip trip, ExpenseCategory category, LocalDate expenseDate,
                       String description, BigDecimal amount) {
        this.id = id;
        this.trip = trip;
        this.category = category;
        this.expenseDate = expenseDate;
        this.description = description;
        this.amount = amount;
    }

    public TripExpense(BusinessTrip trip, ExpenseCategory category, LocalDate expenseDate,
                       String description, BigDecimal amount) {
        this(null, trip, category, expenseDate, description, amount);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusinessTrip getTrip() { return trip; }
    public void setTrip(BusinessTrip trip) { this.trip = trip; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @Override public String toString() {
        return String.format("%d | поездка %d | %s | %s | %s | %s",
                id, trip.getId(), category, expenseDate, description, amount);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TripExpense)) return false;
        TripExpense that = (TripExpense) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return id == null ? 0 : id.hashCode(); }
}
