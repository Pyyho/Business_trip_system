package ru.pkc.travel.model;

import java.math.BigDecimal;

public class TripStatistics {
    private final long total;
    private final long planned;
    private final long active;
    private final long completed;
    private final long cancelled;
    private final BigDecimal totalPlannedCost;
    private final BigDecimal totalActualCost;
    private final double averageDurationDays;

    public TripStatistics(long total, long planned, long active, long completed, long cancelled,
                          BigDecimal totalPlannedCost, BigDecimal totalActualCost,
                          double averageDurationDays) {
        this.total = total;
        this.planned = planned;
        this.active = active;
        this.completed = completed;
        this.cancelled = cancelled;
        this.totalPlannedCost = totalPlannedCost;
        this.totalActualCost = totalActualCost;
        this.averageDurationDays = averageDurationDays;
    }

    public long getTotal() { return total; }
    public long getPlanned() { return planned; }
    public long getActive() { return active; }
    public long getCompleted() { return completed; }
    public long getCancelled() { return cancelled; }
    public BigDecimal getTotalPlannedCost() { return totalPlannedCost; }
    public BigDecimal getTotalActualCost() { return totalActualCost; }
    public double getAverageDurationDays() { return averageDurationDays; }
}
