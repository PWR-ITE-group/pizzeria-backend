package pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.PriceHistoryLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PriceHistoryLogRepositoryTest {

    @Autowired
    private PriceHistoryLogRepository logRepository;


    @Test
    @DisplayName("Functional: Should save Audit Log")
    void shouldSaveLogEntry() {
        PriceHistoryLog log = new PriceHistoryLog();
        log.setProductId(100L); // We don't need a real Product entity for this log table
        log.setOldPrice(new BigDecimal("20.00"));
        log.setNewPrice(new BigDecimal("25.00"));
        log.setChangedBy("manager_bob");
        log.setChangedAt(LocalDateTime.now());

        PriceHistoryLog savedLog = logRepository.save(log);

        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getChangedBy()).isEqualTo("manager_bob");
    }

    @Test
    @DisplayName("Sorting: Should find logs by Product ID ordered by Date DESC")
    void shouldFindLogsSortedByDate() {
        Long productId = 50L;

        logRepository.save(createLog(productId, "10.00", "12.00", LocalDateTime.now().minusDays(5)));
        logRepository.save(createLog(productId, "12.00", "15.00", LocalDateTime.now()));
        logRepository.save(createLog(productId, "15.00", "14.00", LocalDateTime.now().minusDays(2)));

        List<PriceHistoryLog> history = logRepository.findByProductIdOrderByChangedAtDesc(productId);

        assertThat(history).hasSize(3);
        assertThat(history.get(0).getNewPrice()).isEqualByComparingTo("15.00"); // The newest one
        assertThat(history.get(2).getNewPrice()).isEqualByComparingTo("12.00"); // The oldest one
    }


    @Test
    @DisplayName("Performance: Write 10,000 Audit Logs")
    void testAuditPerformance() {
        int count = 10000;
        List<PriceHistoryLog> logs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            logs.add(createLog((long) i, "10", "20", LocalDateTime.now()));
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logRepository.saveAll(logs);
        logRepository.flush();

        stopWatch.stop();

        System.out.println("--------------------------------------------------");
        System.out.println("Audited " + count + " price changes in: " + stopWatch.getTotalTimeMillis() + " ms");
        System.out.println("--------------------------------------------------");

        assertThat(logRepository.count()).isGreaterThanOrEqualTo(count);
    }

    private PriceHistoryLog createLog(Long productId, String oldP, String newP, LocalDateTime date) {
        PriceHistoryLog log = new PriceHistoryLog();
        log.setProductId(productId);
        log.setOldPrice(new BigDecimal(oldP));
        log.setNewPrice(new BigDecimal(newP));
        log.setChangedAt(date);
        log.setChangedBy("test_user");
        return log;
    }
}
