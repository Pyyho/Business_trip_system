package ru.pkc.travel.util;

import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DataExporter {
    Path export(List<Employee> employees, List<BusinessTrip> trips, Path target) throws IOException;
}
