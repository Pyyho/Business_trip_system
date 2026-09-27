package ru.pkc.travel.model;

public enum ExpenseCategory {
    TRANSPORT("Транспорт"),
    ACCOMMODATION("Проживание"),
    MEALS("Питание"),
    COMMUNICATION("Связь"),
    OTHER("Прочее");

    private final String displayName;

    ExpenseCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
    @Override public String toString() { return displayName; }
}
