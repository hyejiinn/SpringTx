package com.example.springtransaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

/**
 * 스프링 트랜잭션 초기화 시점 TEST
 * : 스프링 초기화 시점에는 트랜잭션이 적용되지 않을 수 있다.
 */
@Slf4j
@SpringBootTest
public class InitTxTest {

    @Autowired
    InitClass initClass;

    @Test
    void test() {
        // 초기화 코드는 스프링이 초기화 시점에 호출한다.
    }

    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        InitClass initClass() {
            return new InitClass();
        }
    }


    @Slf4j
    static class InitClass {

        // 초기화 코드와 @Transactional 을 함께 사용하면 트랜잭션이 적용되지 않는다.
        // 초기화 코드가 먼저 호출되고 그 다음에 트랜잭션 AOP가 적용되기 때문이다.
        @PostConstruct
        @Transactional
        public void initV1() {
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("InitClass init @PostConstruct tx Active ={}", transactionActive); // false
        }

        // 트랜잭션 AOP 를 포함한 스프링이 컨테이너가 완전히 생성되고 난 다음에 이벤트가 붙은 메서드를 호출해준다.
        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("InitClass init ApplicationReadyEvent Active={}", transactionActive); // true
        }
    }

}
