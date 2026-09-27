package ru.pkc.travel.repository.jdbc;

import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.model.TransportTicket;
import ru.pkc.travel.model.TransportType;
import ru.pkc.travel.repository.TransportTicketRepository;
import ru.pkc.travel.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTransportTicketRepository implements TransportTicketRepository {
    private static final String SELECT = "SELECT tt.id AS entity_id, tt.transport_type, tt.carrier, " +
            "tt.ticket_number, tt.departure_place, tt.arrival_place, tt.departure_at, tt.arrival_at, tt.cost, " +
            JdbcMapping.TRIP_COLUMNS +
            "FROM transport_tickets tt JOIN business_trips t ON t.id = tt.trip_id " +
            "JOIN employees e ON e.id = t.employee_id ";
    private final DatabaseManager databaseManager;

    public JdbcTransportTicketRepository(DatabaseManager databaseManager) { this.databaseManager = databaseManager; }

    @Override public TransportTicket create(TransportTicket ticket) {
        String sql = "INSERT INTO transport_tickets(trip_id, transport_type, carrier, ticket_number, " +
                "departure_place, arrival_place, departure_at, arrival_at, cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = databaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(s, ticket);
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("База данных не вернула ID.");
                ticket.setId(keys.getLong(1));
            }
            return ticket;
        } catch (SQLException e) { throw error("Не удалось создать билет", e); }
    }

    @Override public List<TransportTicket> findAll() { return query(SELECT + "ORDER BY tt.id", null); }
    @Override public Optional<TransportTicket> findById(long id) {
        List<TransportTicket> rows = query(SELECT + "WHERE tt.id = ?", id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
    @Override public List<TransportTicket> findByTripId(long tripId) {
        return query(SELECT + "WHERE tt.trip_id = ? ORDER BY tt.departure_at", tripId);
    }

    @Override public void update(TransportTicket ticket) {
        String sql = "UPDATE transport_tickets SET trip_id=?, transport_type=?, carrier=?, ticket_number=?, " +
                "departure_place=?, arrival_place=?, departure_at=?, arrival_at=?, cost=? WHERE id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            setFields(s, ticket); s.setLong(10, ticket.getId());
            if (s.executeUpdate() == 0) throw new DataAccessException("Билет не найден.");
        } catch (SQLException e) { throw error("Не удалось обновить билет", e); }
    }

    @Override public void deleteById(long id) { delete("transport_tickets", id); }

    @Override public boolean existsByTicketNumber(String number, Long excludedId) {
        String sql = "SELECT 1 FROM transport_tickets WHERE lower(ticket_number)=lower(?)"
                + (excludedId == null ? "" : " AND id<>?");
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, number); if (excludedId != null) s.setLong(2, excludedId);
            try (ResultSet row = s.executeQuery()) { return row.next(); }
        } catch (SQLException e) { throw error("Не удалось проверить номер билета", e); }
    }

    private List<TransportTicket> query(String sql, Long parameter) {
        List<TransportTicket> result = new ArrayList<>();
        try (Connection c = databaseManager.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            if (parameter != null) s.setLong(1, parameter);
            try (ResultSet rows = s.executeQuery()) { while (rows.next()) result.add(map(rows)); }
            return result;
        } catch (SQLException e) { throw error("Не удалось получить билеты", e); }
    }

    private void delete(String table, long id) {
        try (Connection c = databaseManager.getConnection();
             PreparedStatement s = c.prepareStatement("DELETE FROM " + table + " WHERE id=?")) {
            s.setLong(1, id);
            if (s.executeUpdate() == 0) throw new DataAccessException("Билет не найден.");
        } catch (SQLException e) { throw error("Не удалось удалить билет", e); }
    }

    private static void setFields(PreparedStatement s, TransportTicket t) throws SQLException {
        s.setLong(1, t.getTrip().getId()); s.setString(2, t.getTransportType().name());
        s.setString(3, t.getCarrier()); s.setString(4, t.getTicketNumber());
        s.setString(5, t.getDeparturePlace()); s.setString(6, t.getArrivalPlace());
        s.setTimestamp(7, Timestamp.valueOf(t.getDepartureAt())); s.setTimestamp(8, Timestamp.valueOf(t.getArrivalAt()));
        s.setBigDecimal(9, t.getCost());
    }

    private static TransportTicket map(ResultSet r) throws SQLException {
        return new TransportTicket(r.getLong("entity_id"), JdbcMapping.mapTrip(r),
                TransportType.valueOf(r.getString("transport_type")), r.getString("carrier"),
                r.getString("ticket_number"), r.getString("departure_place"), r.getString("arrival_place"),
                r.getTimestamp("departure_at").toLocalDateTime(), r.getTimestamp("arrival_at").toLocalDateTime(),
                r.getBigDecimal("cost"));
    }

    private static DataAccessException error(String action, SQLException e) {
        return new DataAccessException(action + ": " + e.getMessage(), e);
    }
}
