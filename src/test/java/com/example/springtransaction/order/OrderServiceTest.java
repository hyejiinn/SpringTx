package com.example.springtransaction.order;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 체크 예외 : 비지니스 의미가 있을 때 사용 -> 커밋
 * 언체크 예외 : 복구 불가능한 예외 -> 롤백
 */
@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    /**
     * 사용자 이름 : 정상
     * 모든 프레소스 정상 수행
     * @throws NotEnoughMoneyException
     */
    @Test
    void complete() throws NotEnoughMoneyException {
        // given
        Order order = new Order();
        order.setUsername("정상");

        // when
        orderService.order(order);

        // then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    /**
     * 사용자 이름 : 예외
     * RumtimeException("시스템 예외") 발생
     * 롤백 수행되기 때문에 Order에는 데이터가 비어있다.
     */
    @Test
    void runtimeException() {
        // given
        Order order = new Order();
        order.setUsername("예외");

        // when
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(RuntimeException.class);

        // then : 롤백 됐기 때문에 데이터가 없어야 한다.
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional.isEmpty()).isTrue();
    }

    /**
     * 사용자 이름 : 잔고부족
     * NotEnoughMoneyException("잔고가 부족합니다.") 예외 발생
     * 체크 예외로 커밋이 수행되어 Order 데이터 저장된다.
     */
    @Test
    void bizException() {
        // given
        Order order = new Order();
        order.setUsername("잔고부족");

        // when
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내");
        }

        // then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");

    }
}