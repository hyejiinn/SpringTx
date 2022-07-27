package com.example.springtransaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 스프링 트랜잭션 적용 위치 TEST
 * : 스프링에서 우선순위는 항상 더 구체적이고 자세한 것이 높은 우선순위를 갖는다!!!
 */
@Slf4j
@SpringBootTest
public class TxApplyLevelTest {

    @Autowired
    LevelService levelService;

    @Test
    void levelTest() {
        levelService.write();
        levelService.read();
    }


    @TestConfiguration
    static class TxApplyLevelConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }


    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        // 클래스보다 메서드가 더 구체적이기 때문에 메서드에 있는 @Transactional(readOnly=false) 옵션을 사용한 트랜잭션이 적용됨
        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            printTxInfo();
        }

        // 클래스에 트랜잭션이 적용되어 있기 때문에 메서드는 자동 적용
        public void read() {
            log.info("call read");
            printTxInfo();
        }


        private void printTxInfo() {
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction Active={}", transactionActive);
            boolean transactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly(); // 현재 트랜잭션에 적용된 readOnly 옵션의 값 반환
            log.info("transaction ReadOnly={}", transactionReadOnly);
        }
    }

}
