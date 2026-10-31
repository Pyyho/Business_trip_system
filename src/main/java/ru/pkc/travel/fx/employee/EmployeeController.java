package ru.pkc.travel.fx.employee;

import static ru.pkc.travel.fx.EntityController.Screen.column;

import ru.pkc.travel.fx.*;
import ru.pkc.travel.model.*;

import java.util.List;

public final class EmployeeController extends EntityController<Employee> {
    @Override
    protected Screen<Employee> createScreen(TravelContext c) {
        return new Screen<Employee>(
                "Сотрудники",
                List.of(
                        column("ФИО", Employee::getFullName),
                        column("Email", Employee::getEmail),
                        column("Отдел", Employee::getDepartment)),
                c.employees::findAll,
                Employee::getId,
                c.employees::delete,
                (e, f) -> {
                    var name = f.text("ФИО", e == null ? null : e.getFullName(), 150);
                    var email = f.text("Email", e == null ? null : e.getEmail(), 180);
                    var department = f.text("Отдел", e == null ? null : e.getDepartment(), 120);
                    f.onSave(
                            () -> {
                                var n = name.get();
                                var m = email.get();
                                var d = department.get();
                                return () -> {
                                    if (e == null) {
                                        c.employees.create(n, m, d);
                                    } else {
                                        c.employees.update(e.getId(), n, m, d);
                                    }
                                };
                            });
                },
                2,
                0,
                null);
    }
}
