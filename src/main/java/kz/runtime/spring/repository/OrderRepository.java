package kz.runtime.spring.repository;

import kz.runtime.spring.entity.Order;
import kz.runtime.spring.entity.OrderStatus;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // List<Order> findAll();
    List<Order> findAllByStatusOrderById(OrderStatus status);
}
