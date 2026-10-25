package ru.pkc.travel.fx.tripexpense;

import ru.pkc.travel.fx.*;
import ru.pkc.travel.model.*;
import java.util.List;
import java.util.function.Supplier;
import static ru.pkc.travel.fx.EntityScreen.column;

/** Table columns, filters and edit form for TripExpense. */
public final class TripExpenseController extends EntityController<TripExpense> {
    @Override
    protected EntityScreen<TripExpense> createScreen(TravelContext c, Supplier<EntityReferences> references) {
        return new EntityScreen<TripExpense>("Расходы", List.of(
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
                }, 1, 0, TripExpense::getExpenseDate);
    }
}
