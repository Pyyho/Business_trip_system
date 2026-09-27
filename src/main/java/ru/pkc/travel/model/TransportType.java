package ru.pkc.travel.model;

public enum TransportType {
    PLANE("Самолёт"),
    TRAIN("Поезд"),
    BUS("Автобус"),
    CAR("Автомобиль"),
    OTHER("Другое");

    private final String displayName;

    TransportType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
    @Override public String toString() { return displayName; }
}
