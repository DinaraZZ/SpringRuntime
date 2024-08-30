package kz.runtime.spring.repository;

import kz.runtime.spring.entity.Characteristic;
import kz.runtime.spring.entity.ProductCharacteristic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCharacteristicRepository extends JpaRepository<ProductCharacteristic, Long> {
    List<ProductCharacteristic> findAllByCharacteristic(Characteristic characteristic);
}
