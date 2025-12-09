package pl.edu.pwr.pizzeria.pizzeriabackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.users.EmployeeRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.repository.products.ProductRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PizzeriaBackendApplicationTests {
//
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Test
//    @Sql(scripts = "/data-test.sql")
//    void shouldLoadDataFromSqlScript() {
//        long employeesCount = employeeRepository.count();
//        assertThat(employeesCount).isEqualTo(2); // John и Mike
//
//        Optional<Employee> admin = employeeRepository.findByLogin("admin");
//        assertThat(admin).isPresent();
//        assertThat(admin.get().getName()).isEqualTo("John");
//        assertThat(admin.get().getRole()).isEqualTo("manager");
//    }
//
//    @Test
//    @Sql(scripts = "/data-test.sql")
//    void shouldFindProductByName() {
//        var products = productRepository.findByName("Margherita");
//
//        assertThat(products).isNotEmpty();
//        assertThat(products.get(0).getBasePrice().doubleValue()).isEqualTo(25.00);
//    }
}