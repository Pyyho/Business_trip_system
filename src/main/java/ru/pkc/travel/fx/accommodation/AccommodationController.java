package ru.pkc.travel.fx.accommodation;

import static ru.pkc.travel.fx.EntityScreen.column;

import ru.pkc.travel.fx.*;
import ru.pkc.travel.model.*;

import java.util.List;
import java.util.function.Supplier;

/** Table columns, filters and edit form for Accommodation. */
public final class AccommodationController extends EntityController<Accommodation> {
    @Override
    protected EntityScreen<Accommodation> createScreen(
            TravelContext c, Supplier<EntityReferences> references) {
        return new EntityScreen<Accommodation>(
                "Проживание",
                List.of(
                        column("Поездка", a -> FormController.display(a.getTrip())),
                        column("Гостиница", Accommodation::getHotelName),
                        column("Адрес", Accommodation::getAddress),
                        column("Заезд", Accommodation::getCheckInDate),
                        column("Выезд", Accommodation::getCheckOutDate),
                        column("Стоимость, ₽", Accommodation::getCost)),
                c.accommodations::findAll,
                Accommodation::getId,
                c.accommodations::delete,
                (a, f) -> {
                    var trip =
                            f.choice(
                                    "Поездка",
                                    references.get().trips(),
                                    a == null ? null : a.getTrip());
                    var hotel = f.text("Гостиница", a == null ? null : a.getHotelName(), 180);
                    var address = f.text("Адрес", a == null ? null : a.getAddress(), 300);
                    var in = f.date("Заезд", a == null ? null : a.getCheckInDate());
                    var out = f.date("Выезд", a == null ? null : a.getCheckOutDate());
                    var cost = f.money("Стоимость, ₽", a == null ? null : a.getCost());
                    f.onSave(
                            () -> {
                                var t = trip.get();
                                var h = hotel.get();
                                var d = address.get();
                                var i = in.get();
                                var o = out.get();
                                var v = cost.get();
                                return () -> {
                                    if (a == null) {
                                        c.accommodations.create(t.getId(), h, d, i, o, v);
                                    } else {
                                        c.accommodations.update(
                                                a.getId(), t.getId(), h, d, i, o, v);
                                    }
                                };
                            });
                },
                1,
                0,
                Accommodation::getCheckInDate);
    }
}
