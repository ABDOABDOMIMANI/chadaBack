package com.chada.repository;

import com.chada.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatusNotOrderByCreatedAtDesc(Order.OrderStatus status);
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
}
