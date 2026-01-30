package pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.payments.PaymentCompanyDetails;

import java.util.Optional;

@Repository
public interface PaymentCompanyDetailsRepository extends JpaRepository<PaymentCompanyDetails, Long> {
    Optional<PaymentCompanyDetails> findByPayment_Id(Long paymentId);
    boolean existsByPayment_Id(Long paymentId);
}

