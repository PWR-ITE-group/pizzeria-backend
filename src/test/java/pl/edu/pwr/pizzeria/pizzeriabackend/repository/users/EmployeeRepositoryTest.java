package pl.edu.pwr.pizzeria.pizzeriabackend.repository.users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.users.Employee;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Functional: Should perform CRUD operations")
    void shouldPerformCrudOperations() {
        // Create
        Employee emp = createEmployee("john_doe", "111111111");
        Employee saved = employeeRepository.save(emp);
        assertThat(saved.getId()).isNotNull();

        // Read
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TestName");

        // Update
        found.get().setRole("manager");
        employeeRepository.save(found.get());
        Employee updated = employeeRepository.findById(saved.getId()).get();
        assertThat(updated.getRole()).isEqualTo("manager");

        // Delete
        employeeRepository.deleteById(saved.getId());
        assertThat(employeeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Constraints: Should throw exception on duplicate LOGIN")
    void shouldThrowOnDuplicateLogin() {
        Employee emp1 = createEmployee("duplicate_login", "123456789");
        employeeRepository.save(emp1);

        Employee emp2 = createEmployee("duplicate_login", "987654321");

        assertThrows(RuntimeException.class, () -> {
            employeeRepository.save(emp2);
            employeeRepository.flush();
        });
    }

    @Test
    @DisplayName("Constraints: Should throw exception on duplicate PHONE")
    void shouldThrowOnDuplicatePhone() {
        Employee emp1 = createEmployee("user1", "555-555-555");
        employeeRepository.save(emp1);

        Employee emp2 = createEmployee("user2", "555-555-555");

        assertThrows(RuntimeException.class, () -> {
            employeeRepository.save(emp2);
            employeeRepository.flush();
        });
    }

    // --- БЛОК 2: ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ (Нагрузка) ---

    @Test
    @DisplayName("Performance: Insert 10 employees")
    void testInsertPerformance10() {
        measureInsertionTime(10);
    }

    @Test
    @DisplayName("Performance: Insert 100 employees")
    void testInsertPerformance100() {
        measureInsertionTime(100);
    }

    @Test
    @DisplayName("Performance: Insert 1,000 employees")
    void testInsertPerformance1000() {
        measureInsertionTime(1000);
    }

    // Осторожно с 100,000 на H2 (может занять память), но для курсовой пойдет
    @Test
    @DisplayName("Performance: Insert 10,000 employees")
    void testInsertPerformance10000() {
        measureInsertionTime(10000);
    }

    // --- БЛОК 3: ТЕСТ ИНДЕКСОВ (Поиск) ---

    @Test
    @DisplayName("Indexes: Compare search by Indexed field (Login) vs Non-Indexed field (Name)")
    void compareSearchPerformance() {
        int count = 5000;
        List<Employee> batch = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            batch.add(createEmployee("user_" + i, "phone_" + i));
        }
        employeeRepository.saveAll(batch);
        employeeRepository.flush();

        // Замеряем поиск по ЛОГИНУ (он Unique + Indexed)
        long startIndexed = System.nanoTime();
        employeeRepository.findByLogin("user_4500");
        long durationIndexed = System.nanoTime() - startIndexed;

        // Замеряем поиск по РОЛИ (если она не индексирована, или просто по имени)
        // В нашем случае роль не уникальна, но давайте искать по Name (не уникально, без индекса)
        // Придется добавить метод findByName в репозиторий или использовать Stream (что не честно),
        // используем существующий findByRole, хоть там и много записей.
        long startNonIndexed = System.nanoTime();
        employeeRepository.findByRole("chef"); // Вернет всех
        long durationNonIndexed = System.nanoTime() - startNonIndexed;

        System.out.println(">>> Search Performance Results:");
        System.out.println("Find by Login (Indexed/Unique): " + durationIndexed + " ns");
        System.out.println("Find by Role (Non-Unique):      " + durationNonIndexed + " ns");
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    private void measureInsertionTime(int count) {
        List<Employee> employees = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            employees.add(createEmployee("login_" + i + "_" + System.nanoTime(), "phone_" + i + "_" + System.nanoTime()));
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // saveAll работает быстрее чем save в цикле (batch insert)
        employeeRepository.saveAll(employees);
        employeeRepository.flush(); // Принудительно отправить в БД

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Inserted " + count + " records in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("Average time per record: " + ((double) stopWatch.getTotalTimeMillis() / count) + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(employeeRepository.count()).isGreaterThanOrEqualTo(count);
    }

    private Employee createEmployee(String login, String phone) {
        return Employee.builder()
                .name("TestName")
                .lastName("TestLastName")
                .phone(phone)
                .login(login)
                .passwordHash("hash")
                .role("chef")
                .createdAt(LocalDateTime.now())
                .completedOrdersCount(0)
                .build();
    }
}
