package com.example.springtransaction.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * username에 따른 처리 프로세스
 * 기본 : payStatus을 '완료' 상태로 처리하고 정상 처리된다.
 * 예외 : RuntimeException("시스템 예외") 로 런타임 예외가 발생한다.
 * 잔고부족 : payStatus를 '대기' 상태로 처리한다.
 *           NotEnoughMoneyException("잔고가 부족합니다") 체크 예외가 발생한다.
 *           잔고 부족은 payStatus 를 대기 상태로 두고, 체크 예외가 발생하지만 order 데이터는 커밋되기를 기대한다!!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order 호출");
        orderRepository.save(order);

        log.info("결제 프로세스 진입");
        if (order.getUsername().equals("예외")) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        } else if (order.getUsername().equals("잔고부족")) {
            log.info("잔고 부족 비지니스 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고가 부족합니다.");
        }else {
            log.info("정상 승인");
            order.setPayStatus("완료");
        }
        log.info("결제 프로세스 완료");
    }
}
