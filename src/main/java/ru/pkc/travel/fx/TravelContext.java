package ru.pkc.travel.fx;

import ru.pkc.travel.repository.jdbc.*;
import ru.pkc.travel.service.*;
import ru.pkc.travel.util.*;

/** Composition root: the desktop client uses the same JDBC layer and rules as KR 1. */
public final class TravelContext {
    public final EmployeeService employees;
    public final BusinessTripService trips;
    public final TransportTicketService tickets;
    public final AccommodationService accommodations;
    public final TripExpenseService expenses;

    public TravelContext() {
        DatabaseManager db = new DatabaseManager();
        db.testConnection();
        SchemaInitializer.initialize(db);
        if (db.isDemoDataEnabled()) {
            DemoDataInitializer.initialize(db);
        }
        var employeeRepo = new JdbcEmployeeRepository(db);
        var tripRepo = new JdbcBusinessTripRepository(db);
        employees = new EmployeeService(employeeRepo, tripRepo);
        trips = new BusinessTripService(tripRepo, employeeRepo);
        tickets = new TransportTicketService(new JdbcTransportTicketRepository(db), tripRepo);
        accommodations = new AccommodationService(new JdbcAccommodationRepository(db), tripRepo);
        expenses = new TripExpenseService(new JdbcTripExpenseRepository(db), tripRepo);
    }
}
