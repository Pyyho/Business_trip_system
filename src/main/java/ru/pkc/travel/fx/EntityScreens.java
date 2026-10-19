package ru.pkc.travel.fx;

import ru.pkc.travel.model.*;
import java.util.List;
import static ru.pkc.travel.fx.EntityScreen.column;

/** Column and editor definitions for all five domain entities. */
public final class EntityScreens {
    private EntityScreens() { }
    public record References(List<Employee> employees, List<BusinessTrip> trips) { }
    public static List<EntityScreen<?>> create(TravelContext c, java.util.function.Supplier<References> references) {
        return List.of(
            new EntityScreen<Employee>("Сотрудники", List.of(
                column("ФИО", Employee::getFullName), column("Email", Employee::getEmail), column("Отдел", Employee::getDepartment)),
                c.employees::findAll, Employee::getId, c.employees::delete, (e, f) -> {
                    var name = f.text("ФИО", e == null ? null : e.getFullName(), 150);
                    var email = f.text("Email", e == null ? null : e.getEmail(), 180);
                    var department = f.text("Отдел", e == null ? null : e.getDepartment(), 120);
                    f.onSave(() -> { var n = name.get(); var m = email.get(); var d = department.get();
                        return () -> { if (e == null) c.employees.create(n,m,d); else c.employees.update(e.getId(),n,m,d); }; });
                }, 2, 0, null),
            new EntityScreen<BusinessTrip>("Поездки", List.of(
                column("Сотрудник", t -> t.getEmployee().getFullName()), column("Направление", BusinessTrip::getDestination),
                column("Цель", BusinessTrip::getPurpose), column("Начало", BusinessTrip::getStartDate), column("Окончание", BusinessTrip::getEndDate),
                column("Статус", BusinessTrip::getStatus), column("План, ₽", BusinessTrip::getPlannedCost), column("Факт, ₽", BusinessTrip::getActualCost)),
                c.trips::findAll, BusinessTrip::getId, c.trips::delete, (t, f) -> {
                    var employee = f.choice("Сотрудник", references.get().employees(), t == null ? null : t.getEmployee());
                    var destination = f.text("Направление", t == null ? null : t.getDestination(), 180);
                    var purpose = f.text("Цель", t == null ? null : t.getPurpose(), 500);
                    var start = f.date("Начало", t == null ? null : t.getStartDate());
                    var end = f.date("Окончание", t == null ? null : t.getEndDate());
                    var cost = f.money("Плановая стоимость, ₽", t == null ? null : t.getPlannedCost());
                    f.onSave(() -> { var e = employee.get(); var d = destination.get(); var p = purpose.get(); var s = start.get(); var n = end.get(); var v = cost.get();
                        return () -> { if (t == null) c.trips.create(e.getId(),d,p,s,n,v); else c.trips.update(t.getId(),e.getId(),d,p,s,n,v); }; });
                }, 5, 0, BusinessTrip::getStartDate),
            new EntityScreen<TransportTicket>("Билеты", List.of(
                column("Поездка", t -> FormController.display(t.getTrip())), column("Транспорт", TransportTicket::getTransportType),
                column("Перевозчик", TransportTicket::getCarrier), column("Номер", TransportTicket::getTicketNumber),
                column("Откуда", TransportTicket::getDeparturePlace), column("Куда", TransportTicket::getArrivalPlace),
                column("Отправление", TransportTicket::getDepartureAt), column("Прибытие", TransportTicket::getArrivalAt), column("Стоимость, ₽", TransportTicket::getCost)),
                c.tickets::findAll, TransportTicket::getId, c.tickets::delete, (t, f) -> {
                    var trip = f.choice("Поездка", references.get().trips(), t == null ? null : t.getTrip());
                    var type = f.choice("Транспорт", List.of(TransportType.values()), t == null ? null : t.getTransportType());
                    var carrier = f.text("Перевозчик", t == null ? null : t.getCarrier(), 150);
                    var number = f.text("Номер билета", t == null ? null : t.getTicketNumber(), 80);
                    var from = f.text("Откуда", t == null ? null : t.getDeparturePlace(), 180);
                    var to = f.text("Куда", t == null ? null : t.getArrivalPlace(), 180);
                    var departure = f.time("Отправление", t == null ? null : t.getDepartureAt());
                    var arrival = f.time("Прибытие", t == null ? null : t.getArrivalAt());
                    var cost = f.money("Стоимость, ₽", t == null ? null : t.getCost());
                    f.onSave(() -> { var p = trip.get(); var y = type.get(); var c1 = carrier.get(); var n = number.get(); var a = from.get(); var b = to.get(); var d = departure.get(); var r = arrival.get(); var v = cost.get();
                        return () -> { if (t == null) c.tickets.create(p.getId(),y,c1,n,a,b,d,r,v); else c.tickets.update(t.getId(),p.getId(),y,c1,n,a,b,d,r,v); }; });
                }, 1, 0, t -> t.getDepartureAt().toLocalDate()),
            new EntityScreen<Accommodation>("Проживание", List.of(
                column("Поездка", a -> FormController.display(a.getTrip())), column("Гостиница", Accommodation::getHotelName), column("Адрес", Accommodation::getAddress),
                column("Заезд", Accommodation::getCheckInDate), column("Выезд", Accommodation::getCheckOutDate), column("Стоимость, ₽", Accommodation::getCost)),
                c.accommodations::findAll, Accommodation::getId, c.accommodations::delete, (a, f) -> {
                    var trip = f.choice("Поездка", references.get().trips(), a == null ? null : a.getTrip());
                    var hotel = f.text("Гостиница", a == null ? null : a.getHotelName(), 180);
                    var address = f.text("Адрес", a == null ? null : a.getAddress(), 300);
                    var in = f.date("Заезд", a == null ? null : a.getCheckInDate());
                    var out = f.date("Выезд", a == null ? null : a.getCheckOutDate());
                    var cost = f.money("Стоимость, ₽", a == null ? null : a.getCost());
                    f.onSave(() -> { var t = trip.get(); var h = hotel.get(); var d = address.get(); var i = in.get(); var o = out.get(); var v = cost.get();
                        return () -> { if (a == null) c.accommodations.create(t.getId(),h,d,i,o,v); else c.accommodations.update(a.getId(),t.getId(),h,d,i,o,v); }; });
                }, 1, 0, Accommodation::getCheckInDate),
            new EntityScreen<TripExpense>("Расходы", List.of(
                column("Поездка", e -> FormController.display(e.getTrip())), column("Категория", TripExpense::getCategory),
                column("Дата", TripExpense::getExpenseDate), column("Описание", TripExpense::getDescription), column("Сумма, ₽", TripExpense::getAmount)),
                c.expenses::findAll, TripExpense::getId, c.expenses::delete, (e, f) -> {
                    var trip = f.choice("Поездка", references.get().trips(), e == null ? null : e.getTrip());
                    var category = f.choice("Категория", List.of(ExpenseCategory.values()), e == null ? null : e.getCategory());
                    var date = f.date("Дата", e == null ? null : e.getExpenseDate());
                    var description = f.text("Описание", e == null ? null : e.getDescription(), 300);
                    var amount = f.money("Сумма, ₽", e == null ? null : e.getAmount());
                    f.onSave(() -> { var t = trip.get(); var y = category.get(); var d = date.get(); var s = description.get(); var v = amount.get();
                        return () -> { if (e == null) c.expenses.create(t.getId(),y,d,s,v); else c.expenses.update(e.getId(),t.getId(),y,d,s,v); }; });
                }, 1, 0, TripExpense::getExpenseDate)
        );
    }
}
