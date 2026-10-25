package ru.pkc.travel.fx.businesstrip;

import static ru.pkc.travel.fx.EntityScreen.column;

import ru.pkc.travel.fx.*;
import ru.pkc.travel.model.*;

import java.util.List;
import java.util.function.Supplier;

/** Table columns, filters and edit form for BusinessTrip. */
public final class BusinessTripController extends EntityController<BusinessTrip> {
    @Override
    protected String deletionWarning(BusinessTrip selected) {
        return "\nТакже будут удалены билеты, проживание и расходы этой поездки.";
    }

    @javafx.fxml.FXML
    protected void changeStatus() {
        BusinessTrip trip = selectedItem();
        if (trip == null) {
            return;
        }
        openDialog(
                "Статус поездки • " + trip.getDestination(),
                form -> {
                    var target =
                            form.choice(
                                    "Новый статус",
                                    java.util.Arrays.stream(TripStatus.values())
                                            .filter(trip.getStatus()::canTransitionTo)
                                            .toList(),
                                    null);
                    var actual =
                            form.money(
                                    "Факт, ₽ (только для завершения)", java.math.BigDecimal.ZERO);
                    form.onSave(
                            () -> {
                                var state = target.get();
                                var sum = state == TripStatus.COMPLETED ? actual.get() : null;
                                return () ->
                                        main().context()
                                                .trips
                                                .changeStatus(trip.getId(), state, sum);
                            });
                });
    }

    @Override
    protected EntityScreen<BusinessTrip> createScreen(
            TravelContext c, Supplier<EntityReferences> references) {
        return new EntityScreen<BusinessTrip>(
                "Поездки",
                List.of(
                        column("Сотрудник", t -> t.getEmployee().getFullName()),
                        column("Направление", BusinessTrip::getDestination),
                        column("Цель", BusinessTrip::getPurpose),
                        column("Начало", BusinessTrip::getStartDate),
                        column("Окончание", BusinessTrip::getEndDate),
                        column("Статус", BusinessTrip::getStatus),
                        column("План, ₽", BusinessTrip::getPlannedCost),
                        column("Факт, ₽", BusinessTrip::getActualCost)),
                c.trips::findAll,
                BusinessTrip::getId,
                c.trips::delete,
                (t, f) -> {
                    var employee =
                            f.choice(
                                    "Сотрудник",
                                    references.get().employees(),
                                    t == null ? null : t.getEmployee());
                    var destination =
                            f.text("Направление", t == null ? null : t.getDestination(), 180);
                    var purpose = f.text("Цель", t == null ? null : t.getPurpose(), 500);
                    var start = f.date("Начало", t == null ? null : t.getStartDate());
                    var end = f.date("Окончание", t == null ? null : t.getEndDate());
                    var cost =
                            f.money("Плановая стоимость, ₽", t == null ? null : t.getPlannedCost());
                    f.onSave(
                            () -> {
                                var e = employee.get();
                                var d = destination.get();
                                var p = purpose.get();
                                var s = start.get();
                                var n = end.get();
                                var v = cost.get();
                                return () -> {
                                    if (t == null) {
                                        c.trips.create(e.getId(), d, p, s, n, v);
                                    } else {
                                        c.trips.update(t.getId(), e.getId(), d, p, s, n, v);
                                    }
                                };
                            });
                },
                5,
                0,
                BusinessTrip::getStartDate);
    }
}
