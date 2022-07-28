package com.example.springtransaction.order;

import org.springframework.data.jpa.repository.JpaRepository;

// 스프링 데이터 JPA 사용
public interface OrderRepository extends JpaRepository<Order, Long> {
}
