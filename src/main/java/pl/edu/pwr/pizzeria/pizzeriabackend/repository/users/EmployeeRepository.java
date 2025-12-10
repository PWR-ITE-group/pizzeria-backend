package pl.edu.pwr.pizzeria.pizzeriabackend.repository.users;

import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final DataSource dataSource;

    public EmployeeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // --- CRUD Operations ---

    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            return insert(employee);
        } else {
            return update(employee);
        }
    }

    // [FIX] Добавлен метод saveAll
    public void saveAll(List<Employee> employees) {
        for (Employee emp : employees) {
            save(emp);
        }
    }

    public Optional<Employee> findById(Long id) {
        String sql = "SELECT * FROM pizzeria_schema.employees WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public List<Employee> findAll() {
        String sql = "SELECT * FROM pizzeria_schema.employees";
        List<Employee> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRowToEmployee(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM pizzeria_schema.employees WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM pizzeria_schema.employees";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- [FIX] Missing methods for Tests ---

    // [FIX] Добавлен метод count
    public long count() {
        String sql = "SELECT COUNT(*) FROM pizzeria_schema.employees";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // [FIX] Добавлен метод flush (в JDBC он пустой, так как запись идет сразу)
    public void flush() {
        // No-op for JDBC as data is committed immediately
    }

    // --- Custom Queries ---

    public Optional<Employee> findByLogin(String login) {
        String sql = "SELECT * FROM pizzeria_schema.employees WHERE login = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public boolean existsByLogin(String login) {
        String sql = "SELECT count(*) FROM pizzeria_schema.employees WHERE login = ?";
        return checkExists(sql, login);
    }

    public boolean existsByPhone(String phone) {
        String sql = "SELECT count(*) FROM pizzeria_schema.employees WHERE phone = ?";
        return checkExists(sql, phone);
    }

    // [FIX] Добавлен метод findByRole
    public List<Employee> findByRole(String role) {
        String sql = "SELECT * FROM pizzeria_schema.employees WHERE role = ?";
        List<Employee> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToEmployee(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // --- Helpers ---

    private boolean checkExists(String sql, String param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private Employee insert(Employee employee) {
        String sql = """
                INSERT INTO pizzeria_schema.employees 
                (name, last_name, phone, login, password_hash, role, created_at, completed_orders_count) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getLastName());
            ps.setString(3, employee.getPhone());
            ps.setString(4, employee.getLogin());
            ps.setString(5, employee.getPasswordHash());
            ps.setString(6, employee.getRole());
            ps.setObject(7, employee.getCreatedAt());
            ps.setInt(8, employee.getCompletedOrdersCount() != null ? employee.getCompletedOrdersCount() : 0);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    employee.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }
            return employee;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Employee update(Employee employee) {
        String sql = """
                UPDATE pizzeria_schema.employees 
                SET name = ?, last_name = ?, phone = ?, login = ?, role = ?, completed_orders_count = ?
                WHERE id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getLastName());
            ps.setString(3, employee.getPhone());
            ps.setString(4, employee.getLogin());
            ps.setString(5, employee.getRole());
            ps.setInt(6, employee.getCompletedOrdersCount() != null ? employee.getCompletedOrdersCount() : 0);
            ps.setLong(7, employee.getId());

            ps.executeUpdate();
            return employee;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getLong("id"));
        emp.setName(rs.getString("name"));
        emp.setLastName(rs.getString("last_name"));
        emp.setPhone(rs.getString("phone"));
        emp.setLogin(rs.getString("login"));
        emp.setPasswordHash(rs.getString("password_hash"));
        emp.setRole(rs.getString("role"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            emp.setCreatedAt(ts.toLocalDateTime());
        }

        emp.setCompletedOrdersCount(rs.getInt("completed_orders_count"));
        return emp;
    }
}
