package ru.pkc.travel.fx;

import java.util.List;
import java.util.function.*;

/** Typed description of an entity screen, without SQL or duplicated table handlers. */
public record EntityScreen<T>(
        String title,
        List<Column<T>> columns,
        Supplier<List<T>> load,
        ToLongFunction<T> id,
        LongConsumer delete,
        BiConsumer<T, FormController> form,
        int firstFilter,
        int secondFilter,
        Function<T, java.time.LocalDate> date) {
    public record Column<T>(String title, Function<T, Object> value) {}

    public static <T> Column<T> column(String title, Function<T, Object> value) {
        return new Column<>(title, value);
    }
}
