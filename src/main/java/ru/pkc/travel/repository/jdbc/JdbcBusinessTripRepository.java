package ru.pkc.travel.repository.jdbc;

import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.model.TripStatus;
import ru.pkc.travel.repository.BusinessTripRepository;
import ru.pkc.travel.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBusinessTripRepository implements BusinessTripRepository {
    private static final String SELECT =
            "SELECT t.id AS trip_id, t.destination, t.purpose, t.start_date, t.end_date, " +
            "t.status, t.planned_cost, t.actual_cost, t.created_at, " +
            "e.id AS employee_id, e.full_name, e.email, e.department " +
            "FROM business_trips t JOIN employees e ON e.id = t.employee_id ";

    private final DatabaseManager databaseManager;

    public JdbcBusinessTripRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public BusinessTrip create(BusinessTrip trip) {
        String sql = "INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, " +
                "status, planned_cost, actual_cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(statement, trip);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("База данных не вернула ID.");
                trip.setId(keys.getLong(1));
            }
            return trip;
        } catch (SQLException e) {
            throw translate("Не удалось создать поездку", e);
        }
    }

    @Override
    public List<BusinessTrip> findAll() {
        return queryList(SELECT + "ORDER BY t.id", null);
    }

    @Override
    public Optional<BusinessTrip> findById(long id) {
        String sql = SELECT + "WHERE t.id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? Optional.of(map(row)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw translate("Не удалось найти поездку", e);
        }
    }

    @Override
    public void update(BusinessTrip trip) {
        String sql = "UPDATE business_trips SET employee_id = ?, destination = ?, purpose = ?, " +
                "start_date = ?, end_date = ?, status = ?, planned_cost = ?, actual_cost = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setFields(statement, trip);
            statement.setLong(9, trip.getId());
            if (statement.executeUpdate() == 0) throw new DataAccessException("Поездка не найдена.");
        } catch (SQLException e) {
            throw translate("Не удалось обновить поездку", e);
        }
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM business_trips WHERE id = ?")) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new DataAccessException("Поездка не найдена.");
        } catch (SQLException e) {
            throw translate("Не удалось удалить поездку", e);
        }
    }

    @Override
    public List<BusinessTrip> searchByDestination(String query) {
        return queryList(SELECT + "WHERE lower(t.destination) LIKE lower(?) ORDER BY t.start_date", "%" + query + "%");
    }

    @Override
    public List<BusinessTrip> searchByEmployee(String query) {
        return queryList(SELECT + "WHERE lower(e.full_name) LIKE lower(?) ORDER BY t.start_date", "%" + query + "%");
    }

    @Override
    public List<BusinessTrip> findByStatus(TripStatus status) {
        return queryList(SELECT + "WHERE t.status = ? ORDER BY t.start_date", status.name());
    }

    @Override
    public List<BusinessTrip> findByDateRange(LocalDate from, LocalDate to) {
        String sql = SELECT + "WHERE t.start_date <= ? AND t.end_date >= ? ORDER BY t.start_date";
        List<BusinessTrip> result = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(to));
            statement.setDate(2, Date.valueOf(from));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(map(rows));
            }
            return result;
        } catch (SQLException e) {
            throw translate("Не удалось отфильтровать поездки по датам", e);
        }
    }

    @Override
    public boolean existsByEmployeeId(long employeeId) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM business_trips WHERE employee_id = ? LIMIT 1")) {
            statement.setLong(1, employeeId);
            try (ResultSet row = statement.executeQuery()) { return row.next(); }
        } catch (SQLException e) {
            throw translate("Не удалось проверить поездки сотрудника", e);
        }
    }

    private List<BusinessTrip> queryList(String sql, String parameter) {
        List<BusinessTrip> result = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) statement.setString(1, parameter);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(map(rows));
            }
            return result;
        } catch (SQLException e) {
            throw translate("Не удалось получить поездки", e);
        }
    }

    private static void setFields(PreparedStatement statement, BusinessTrip trip) throws SQLException {
        statement.setLong(1, trip.getEmployee().getId());
        statement.setString(2, trip.getDestination());
        statement.setString(3, trip.getPurpose());
        statement.setDate(4, Date.valueOf(trip.getStartDate()));
        statement.setDate(5, Date.valueOf(trip.getEndDate()));
        statement.setString(6, trip.getStatus().name());
        statement.setBigDecimal(7, trip.getPlannedCost());
        if (trip.getActualCost() == null) statement.setNull(8, java.sql.Types.NUMERIC);
        else statement.setBigDecimal(8, trip.getActualCost());
    }

    private static BusinessTrip map(ResultSet row) throws SQLException {
        Employee employee = new Employee(row.getLong("employee_id"), row.getString("full_name"),
                row.getString("email"), row.getString("department"));
        Timestamp createdAt = row.getTimestamp("created_at");
        return new BusinessTrip(row.getLong("trip_id"), employee, row.getString("destination"),
                row.getString("purpose"), row.getDate("start_date").toLocalDate(),
                row.getDate("end_date").toLocalDate(), TripStatus.valueOf(row.getString("status")),
                row.getBigDecimal("planned_cost"), row.getBigDecimal("actual_cost"),
                createdAt == null ? null : createdAt.toLocalDateTime());
    }

    private static DataAccessException translate(String action, SQLException e) {
        return new DataAccessException(action + ": " + e.getMessage(), e);
    }
}
