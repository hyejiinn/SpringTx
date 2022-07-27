package com.example.springtransaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *  스프링 트랜잭션 적용 확인 Test
 *  : 선언적 트랜잭션은 AOP 기반으로 동작하기 때문에 실제로 트랜잭션이 적용되고 있는지 아닌지 확인이 어렵다.
 *  따라서 이 테스트를 통해서 트랜잭션이 적용되는지 확인해본다.
 *
 * @Transactional 애노테이션이 특정 클래스나 메서드에 하나라도 붙어있으면 트랜잭션 AOP는 프록시를 만들어서 스프링 컨테이너에 등록한다.
 */
@Slf4j
@SpringBootTest
public class TxApplyBasicTest {

    @Autowired
    BasicService basicService;

    /**
     * AopUtils.isAopProxy() : true
     * 선언적 트랜잭션 방식(@Transactional) 에서는 스프링 트랜잭션은 AOP 기반으로 동작한다. 그러므로 true
     */
    @Test
    void proxyCheck() {
        log.info("aop class={}", basicService.getClass()); // TxApplyBasicTest$BasicService$$EnhancerBySpringCGLIB$$74ca9b49
        Assertions.assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }


    @Slf4j
    static class BasicService {

        @Transactional
        public void tx() {
            log.info("call transaction");
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive(); // 현재 쓰레드에 트랜잭션이 적용되어 있는지 확인할 수 있는 기능
            log.info("transaction Active={}", transactionActive);
        }

        public void nonTx() {
            log.info("call NonTransaction");
            boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transaction Active={}", transactionActive);
        }
    }

}

