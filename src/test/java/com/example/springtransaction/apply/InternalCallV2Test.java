package com.example.springtransaction.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 트랜잭션 AOP 주의 사항 - 프록시 내부 호출⭐⭐⭐⭐⭐
 * @Transactional를 사용하는 트랜잭션 AOP는 프록시를 사용한다. 프록시를 사용하면 메서드 내부 호출에 프록시를 적용할 수 없다.
 * -> 해결 방법은 해당 메서드를 별도의 클래스로 분리하는 방법이다.
 */
@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService callService;

    @Test
    void externalCall() {
        callService.external();
    }


    @TestConfiguration
    static class InternalCallV1Config {
        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }


    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("call external");
            printTxInfo();
            internalService.internal(); // this.internal(); 실제 target에 있는 internal 호출, 즉 내부 호출
        }
        private void printTxInfo() {
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction Active={}", transactionActive);
        }
    }

    @Slf4j
    static class InternalService {

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction Active={}", transactionActive);
        }
    }


}
