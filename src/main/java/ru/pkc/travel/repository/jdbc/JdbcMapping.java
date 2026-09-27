package ru.pkc.travel.repository.jdbc;

import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.TripStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

final class JdbcMapping {
    static final String TRIP_COLUMNS =
            "t.id AS trip_id, t.destination AS trip_destination, t.purpose AS trip_purpose, " +
            "t.start_date AS trip_start_date, t.end_date AS trip_end_date, t.status AS trip_status, " +
            "t.planned_cost AS trip_planned_cost, t.actual_cost AS trip_actual_cost, " +
            "t.created_at AS trip_created_at, e.id AS employee_id, e.full_name, e.email, e.department ";

    private JdbcMapping() { }

    static BusinessTrip mapTrip(ResultSet row) throws SQLException {
        Employee employee = new Employee(row.getLong("employee_id"), row.getString("full_name"),
                row.getString("email"), row.getString("department"));
        Timestamp createdAt = row.getTimestamp("trip_created_at");
        return new BusinessTrip(row.getLong("trip_id"), employee, row.getString("trip_destination"),
                row.getString("trip_purpose"), row.getDate("trip_start_date").toLocalDate(),
                row.getDate("trip_end_date").toLocalDate(), TripStatus.valueOf(row.getString("trip_status")),
                row.getBigDecimal("trip_planned_cost"), row.getBigDecimal("trip_actual_cost"),
                createdAt == null ? null : createdAt.toLocalDateTime());
    }
}
