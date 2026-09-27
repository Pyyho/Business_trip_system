package ru.pkc.travel.repository.jdbc;

import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.model.Accommodation;
import ru.pkc.travel.repository.AccommodationRepository;
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

public class JdbcAccommodationRepository implements AccommodationRepository {
    private static final String SELECT = "SELECT a.id AS entity_id, a.hotel_name, a.address, " +
            "a.check_in_date, a.check_out_date, a.cost, " + JdbcMapping.TRIP_COLUMNS +
            "FROM accommodations a JOIN business_trips t ON t.id=a.trip_id " +
            "JOIN employees e ON e.id=t.employee_id ";
    private final DatabaseManager databaseManager;

    public JdbcAccommodationRepository(DatabaseManager databaseManager) { this.databaseManager = databaseManager; }

    @Override public Accommodation create(Accommodation value) {
        String sql = "INSERT INTO accommodations(trip_id, hotel_name, address, check_in_date, check_out_date, cost) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = databaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(s, value); s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("База данных не вернула ID.");
                value.setId(keys.getLong(1));
            }
            return value;
        } catch (SQLException e) { throw error("Не удалось создать бронирование", e); }
    }

    @Override public List<Accommodation> findAll() { return query(SELECT + "ORDER BY a.id", null); }
    @Override public Optional<Accommodation> findById(long id) {
        List<Accommodation> rows = query(SELECT + "WHERE a.id=?", id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
    @Override public List<Accommodation> findByTripId(long tripId) {
        return query(SELECT + "WHERE a.trip_id=? ORDER BY a.check_in_date", tripId);
    }
    @Override public void update(Accommodation value) {
        String sql = "UPDATE accommodations SET trip_id=?, hotel_name=?, address=?, check_in_date=?, " +
                "check_out_date=?, cost=? WHERE id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            setFields(s, value); s.setLong(7, value.getId());
            if (s.executeUpdate() == 0) throw new DataAccessException("Проживание не найдено.");
        } catch (SQLException e) { throw error("Не удалось обновить проживание", e); }
    }
    @Override public void deleteById(long id) {
        try (Connection c = databaseManager.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM accommodations WHERE id=?")) {
            s.setLong(1, id);
            if (s.executeUpdate() == 0) throw new DataAccessException("Проживание не найдено.");
        } catch (SQLException e) { throw error("Не удалось удалить проживание", e); }
    }

    private List<Accommodation> query(String sql, Long parameter) {
        List<Accommodation> result = new ArrayList<>();
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            if (parameter != null) s.setLong(1, parameter);
            try (ResultSet rows = s.executeQuery()) { while (rows.next()) result.add(map(rows)); }
            return result;
        } catch (SQLException e) { throw error("Не удалось получить проживание", e); }
    }

    private static void setFields(PreparedStatement s, Accommodation a) throws SQLException {
        s.setLong(1, a.getTrip().getId()); s.setString(2, a.getHotelName()); s.setString(3, a.getAddress());
        s.setDate(4, Date.valueOf(a.getCheckInDate())); s.setDate(5, Date.valueOf(a.getCheckOutDate()));
        s.setBigDecimal(6, a.getCost());
    }
    private static Accommodation map(ResultSet r) throws SQLException {
        return new Accommodation(r.getLong("entity_id"), JdbcMapping.mapTrip(r), r.getString("hotel_name"),
                r.getString("address"), r.getDate("check_in_date").toLocalDate(),
                r.getDate("check_out_date").toLocalDate(), r.getBigDecimal("cost"));
    }
    private static DataAccessException error(String action, SQLException e) {
        return new DataAccessException(action + ": " + e.getMessage(), e);
    }
}
