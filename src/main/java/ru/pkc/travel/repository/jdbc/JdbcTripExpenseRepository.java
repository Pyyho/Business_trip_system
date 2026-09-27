package ru.pkc.travel.repository.jdbc;

import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.model.ExpenseCategory;
import ru.pkc.travel.model.TripExpense;
import ru.pkc.travel.repository.TripExpenseRepository;
import ru.pkc.travel.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTripExpenseRepository implements TripExpenseRepository {
    private static final String SELECT = "SELECT x.id AS entity_id, x.category, x.expense_date, " +
            "x.description, x.amount, " + JdbcMapping.TRIP_COLUMNS +
            "FROM trip_expenses x JOIN business_trips t ON t.id=x.trip_id " +
            "JOIN employees e ON e.id=t.employee_id ";
    private final DatabaseManager databaseManager;

    public JdbcTripExpenseRepository(DatabaseManager databaseManager) { this.databaseManager = databaseManager; }

    @Override public TripExpense create(TripExpense value) {
        String sql = "INSERT INTO trip_expenses(trip_id, category, expense_date, description, amount) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = databaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(s, value); s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("База данных не вернула ID.");
                value.setId(keys.getLong(1));
            }
            return value;
        } catch (SQLException e) { throw error("Не удалось создать расход", e); }
    }
    @Override public List<TripExpense> findAll() { return query(SELECT + "ORDER BY x.id", null); }
    @Override public Optional<TripExpense> findById(long id) {
        List<TripExpense> rows = query(SELECT + "WHERE x.id=?", id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
    @Override public List<TripExpense> findByTripId(long tripId) {
        return query(SELECT + "WHERE x.trip_id=? ORDER BY x.expense_date", tripId);
    }
    @Override public void update(TripExpense value) {
        String sql = "UPDATE trip_expenses SET trip_id=?, category=?, expense_date=?, description=?, amount=? WHERE id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            setFields(s, value); s.setLong(6, value.getId());
            if (s.executeUpdate() == 0) throw new DataAccessException("Расход не найден.");
        } catch (SQLException e) { throw error("Не удалось обновить расход", e); }
    }
    @Override public void deleteById(long id) {
        try (Connection c = databaseManager.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM trip_expenses WHERE id=?")) {
            s.setLong(1, id);
            if (s.executeUpdate() == 0) throw new DataAccessException("Расход не найден.");
        } catch (SQLException e) { throw error("Не удалось удалить расход", e); }
    }

    private List<TripExpense> query(String sql, Long parameter) {
        List<TripExpense> result = new ArrayList<>();
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            if (parameter != null) s.setLong(1, parameter);
            try (ResultSet rows = s.executeQuery()) { while (rows.next()) result.add(map(rows)); }
            return result;
        } catch (SQLException e) { throw error("Не удалось получить расходы", e); }
    }

    private static void setFields(PreparedStatement s, TripExpense x) throws SQLException {
        s.setLong(1, x.getTrip().getId()); s.setString(2, x.getCategory().name());
        s.setDate(3, Date.valueOf(x.getExpenseDate())); s.setString(4, x.getDescription());
        s.setBigDecimal(5, x.getAmount());
    }
    private static TripExpense map(ResultSet r) throws SQLException {
        return new TripExpense(r.getLong("entity_id"), JdbcMapping.mapTrip(r),
                ExpenseCategory.valueOf(r.getString("category")), r.getDate("expense_date").toLocalDate(),
                r.getString("description"), r.getBigDecimal("amount"));
    }
    private static DataAccessException error(String action, SQLException e) {
        return new DataAccessException(action + ": " + e.getMessage(), e);
    }
}
