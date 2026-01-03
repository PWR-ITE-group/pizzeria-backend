package pl.edu.pwr.pizzeria.pizzeriabackend.repository.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pwr.pizzeria.pizzeriabackend.model.entity.deliveries.DeliveryInfo;

import java.util.List;

@Repository
public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfo, Long> {
    // Найти клиента по телефону (для автозаполнения при звонке)
    List<DeliveryInfo> findByPhone(String phone);
    
    // Найти информацию о доставке по ID доставки
    java.util.Optional<DeliveryInfo> findByDelivery_Id(Long deliveryId);
}
