package ru.pkc.travel.fx;

import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;

import java.util.List;

public record EntityReferences(List<Employee> employees, List<BusinessTrip> trips) {}
