package pl.edu.pwr.pizzeria.pizzeriabackend.repository.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.PaymentCompanyDetails;

public interface PaymentCompanyDetailsRepository extends JpaRepository<PaymentCompanyDetails, Long> {
}
