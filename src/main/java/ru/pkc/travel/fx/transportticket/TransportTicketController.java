package ru.pkc.travel.fx.transportticket;

import static ru.pkc.travel.fx.EntityScreen.column;

import ru.pkc.travel.fx.*;
import ru.pkc.travel.model.*;

import java.util.List;
import java.util.function.Supplier;

/** Table columns, filters and edit form for TransportTicket. */
public final class TransportTicketController extends EntityController<TransportTicket> {
    @Override
    protected EntityScreen<TransportTicket> createScreen(
            TravelContext c, Supplier<EntityReferences> references) {
        return new EntityScreen<TransportTicket>(
                "Билеты",
                List.of(
                        column("Поездка", t -> FormController.display(t.getTrip())),
                        column("Транспорт", TransportTicket::getTransportType),
                        column("Перевозчик", TransportTicket::getCarrier),
                        column("Номер", TransportTicket::getTicketNumber),
                        column("Откуда", TransportTicket::getDeparturePlace),
                        column("Куда", TransportTicket::getArrivalPlace),
                        column("Отправление", TransportTicket::getDepartureAt),
                        column("Прибытие", TransportTicket::getArrivalAt),
                        column("Стоимость, ₽", TransportTicket::getCost)),
                c.tickets::findAll,
                TransportTicket::getId,
                c.tickets::delete,
                (t, f) -> {
                    var trip =
                            f.choice(
                                    "Поездка",
                                    references.get().trips(),
                                    t == null ? null : t.getTrip());
                    var type =
                            f.choice(
                                    "Транспорт",
                                    List.of(TransportType.values()),
                                    t == null ? null : t.getTransportType());
                    var carrier = f.text("Перевозчик", t == null ? null : t.getCarrier(), 150);
                    var number = f.text("Номер билета", t == null ? null : t.getTicketNumber(), 80);
                    var from = f.text("Откуда", t == null ? null : t.getDeparturePlace(), 180);
                    var to = f.text("Куда", t == null ? null : t.getArrivalPlace(), 180);
                    var departure = f.time("Отправление", t == null ? null : t.getDepartureAt());
                    var arrival = f.time("Прибытие", t == null ? null : t.getArrivalAt());
                    var cost = f.money("Стоимость, ₽", t == null ? null : t.getCost());
                    f.onSave(
                            () -> {
                                var p = trip.get();
                                var y = type.get();
                                var c1 = carrier.get();
                                var n = number.get();
                                var a = from.get();
                                var b = to.get();
                                var d = departure.get();
                                var r = arrival.get();
                                var v = cost.get();
                                return () -> {
                                    if (t == null) {
                                        c.tickets.create(p.getId(), y, c1, n, a, b, d, r, v);
                                    } else {
                                        c.tickets.update(
                                                t.getId(), p.getId(), y, c1, n, a, b, d, r, v);
                                    }
                                };
                            });
                },
                1,
                0,
                t -> t.getDepartureAt().toLocalDate());
    }
}
