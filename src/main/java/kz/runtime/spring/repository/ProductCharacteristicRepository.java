package kz.runtime.spring.repository;

import kz.runtime.spring.entity.ProductCharacteristic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCharacteristicRepository extends JpaRepository<ProductCharacteristic, Long> {
}
