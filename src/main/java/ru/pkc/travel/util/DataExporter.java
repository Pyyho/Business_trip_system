package ru.pkc.travel.util;

import ru.pkc.travel.model.Accommodation;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.TransportTicket;
import ru.pkc.travel.model.TripExpense;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DataExporter {
    Path export(List<Employee> employees, List<BusinessTrip> trips,
                List<TransportTicket> tickets, List<Accommodation> accommodations,
                List<TripExpense> expenses, Path target) throws IOException;
}
