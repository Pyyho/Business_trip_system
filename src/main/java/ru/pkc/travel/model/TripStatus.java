package ru.pkc.travel.model;

import java.util.EnumSet;
import java.util.Set;

public enum TripStatus {
    PLANNED("Запланирована"),
    APPROVED("Согласована"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String displayName;

    TripStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(TripStatus target) {
        Set<TripStatus> allowed;
        switch (this) {
            case PLANNED:
                allowed = EnumSet.of(APPROVED, CANCELLED);
                break;
            case APPROVED:
                allowed = EnumSet.of(IN_PROGRESS, CANCELLED);
                break;
            case IN_PROGRESS:
                allowed = EnumSet.of(COMPLETED);
                break;
            default:
                allowed = EnumSet.noneOf(TripStatus.class);
        }
        return allowed.contains(target);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
