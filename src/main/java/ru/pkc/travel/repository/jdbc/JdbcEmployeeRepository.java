package ru.pkc.travel.repository.jdbc;

import ru.pkc.travel.exception.DataAccessException;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.repository.EmployeeRepository;
import ru.pkc.travel.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmployeeRepository implements EmployeeRepository {
    private final DatabaseManager databaseManager;

    public JdbcEmployeeRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Employee create(Employee employee) {
        String sql = "INSERT INTO employees(full_name, email, department) VALUES (?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(statement, employee);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("База данных не вернула ID.");
                employee.setId(keys.getLong(1));
            }
            return employee;
        } catch (SQLException e) {
            throw translate("Не удалось создать сотрудника", e);
        }
    }

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT id, full_name, email, department FROM employees ORDER BY id";
        List<Employee> result = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(map(rows));
            return result;
        } catch (SQLException e) {
            throw translate("Не удалось получить список сотрудников", e);
        }
    }

    @Override
    public Optional<Employee> findById(long id) {
        String sql = "SELECT id, full_name, email, department FROM employees WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? Optional.of(map(row)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw translate("Не удалось найти сотрудника", e);
        }
    }

    @Override
    public void update(Employee employee) {
        String sql = "UPDATE employees SET full_name = ?, email = ?, department = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setFields(statement, employee);
            statement.setLong(4, employee.getId());
            if (statement.executeUpdate() == 0) throw new DataAccessException("Сотрудник не найден.");
        } catch (SQLException e) {
            throw translate("Не удалось обновить сотрудника", e);
        }
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new DataAccessException("Сотрудник не найден.");
        } catch (SQLException e) {
            throw translate("Не удалось удалить сотрудника", e);
        }
    }

    @Override
    public boolean existsByEmail(String email, Long excludedId) {
        String sql = "SELECT 1 FROM employees WHERE lower(email) = lower(?)"
                + (excludedId == null ? "" : " AND id <> ?");
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            if (excludedId != null) statement.setLong(2, excludedId);
            try (ResultSet row = statement.executeQuery()) { return row.next(); }
        } catch (SQLException e) {
            throw translate("Не удалось проверить email", e);
        }
    }

    private static void setFields(PreparedStatement statement, Employee employee) throws SQLException {
        statement.setString(1, employee.getFullName());
        statement.setString(2, employee.getEmail());
        statement.setString(3, employee.getDepartment());
    }

    private static Employee map(ResultSet row) throws SQLException {
        return new Employee(row.getLong("id"), row.getString("full_name"),
                row.getString("email"), row.getString("department"));
    }

    private static DataAccessException translate(String action, SQLException e) {
        return new DataAccessException(action + ": " + e.getMessage(), e);
    }
}
