package ru.pkc.travel.fx;

import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;

import java.util.List;

/** Related records loaded before opening an entity form. */
public record EntityReferences(List<Employee> employees, List<BusinessTrip> trips) {}
