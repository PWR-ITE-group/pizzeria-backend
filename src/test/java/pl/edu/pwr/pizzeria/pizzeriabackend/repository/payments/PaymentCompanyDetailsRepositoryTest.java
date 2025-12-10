package pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.Payment;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.PaymentCompanyDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentCompanyDetailsRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentCompanyDetailsRepository paymentCompanyDetailsRepository;

    @Test
    @DisplayName("Cascade Save: Should save Details automatically when saving Payment")
    void shouldSaveDetailsViaCascade() {
        Payment payment = Payment.builder()
                .amount(new BigDecimal("120.50"))
                .method("transfer")
                .status("pending")
                .paidAt(LocalDateTime.now())
                .build();

        PaymentCompanyDetails details = PaymentCompanyDetails.builder()
                .companyName("Tech Solutions Sp. z o.o.")
                .nip("898-123-45-67")
                .street("Grunwaldzka")
                .houseNr("10")
                .city("Wroclaw")
                .postalCode("50-300")
                .build();

        payment.setCompanyDetails(details);

        details.setPayment(payment);

        Payment savedPayment = paymentRepository.save(payment);

        assertThat(savedPayment.getId()).isNotNull();

        assertThat(savedPayment.getCompanyDetails().getId()).isNotNull();

        Optional<PaymentCompanyDetails> foundDetails = paymentCompanyDetailsRepository.findById(savedPayment.getCompanyDetails().getId());
        assertThat(foundDetails).isPresent();
        assertThat(foundDetails.get().getCompanyName()).isEqualTo("Tech Solutions Sp. z o.o.");
        assertThat(foundDetails.get().getPayment().getId()).isEqualTo(savedPayment.getId());
    }

    @Test
    @DisplayName("Cascade Delete: Should delete Details when Payment is deleted")
    void shouldDeleteDetailsOnPaymentDelete() {
        Payment payment = Payment.builder().amount(BigDecimal.TEN).method("cash").status("paid").build();
        PaymentCompanyDetails details = PaymentCompanyDetails.builder().companyName("Small Shop").nip("111-222-33-44").build();

        payment.setCompanyDetails(details);
        details.setPayment(payment);

        Payment savedPayment = paymentRepository.save(payment);
        Long detailsId = savedPayment.getCompanyDetails().getId();

        assertThat(paymentCompanyDetailsRepository.existsById(detailsId)).isTrue();

        paymentRepository.deleteById(savedPayment.getId());
        paymentRepository.flush();

        assertThat(paymentRepository.existsById(savedPayment.getId())).isFalse();
        assertThat(paymentCompanyDetailsRepository.existsById(detailsId)).isFalse();
    }

    @Test
    @DisplayName("Constraints: Should fail if required fields (NIP) are missing")
    void shouldFailWithoutRequiredFields() {
        Payment payment = Payment.builder().amount(BigDecimal.TEN).method("card").status("new").build();

        PaymentCompanyDetails invalidDetails = PaymentCompanyDetails.builder()
                .companyName("No NIP Company")
                .build();

        payment.setCompanyDetails(invalidDetails);
        invalidDetails.setPayment(payment);

        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.saveAndFlush(payment);
        });
    }
}