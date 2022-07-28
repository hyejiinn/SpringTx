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
 * 트랜잭션 AOP 주의 사항 - 프록시 내부 호출!!!!!!
 * : 실무에서 많이 마주치는 문제니까 꼭 알아두기 ⭐⭐⭐⭐⭐⭐
 *
 * @Transactional을 사용하면 스프링 트랜잭션 AOP가 적용된다.
 * 트랜잭션 AOP는 기본적으로 프록시 방식의 AOP를 사용한다.
 * -> 즉 @Transactional을 적용하면 프록시 객체가 요청을 먼저 받아서 트랜잭션을 처리하고 실제 객체를 호출해준다.
 * 따라서 트랜잭션을 적용하기 위해서는 항상 프록시를 통해서 대상(Target)을 호출해야 한다.
 * 만약 프록시를 거치지 않고 대상 객체를 직접 호출한다면 AOP가 적용되지 않고 트랜잭션도 적용되지 않는다.
 */
@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    // @Transactional이 하나라도 있으면 트랜잭션 프록시 객체가 만들어진다.
    // 따라서 callService빈을 주입 받으면 프록시 객체가 대신 주입된다.
    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass()); //InternalCallV1Test$CallService$$EnhancerBySpringCGLIB$$a9b971af
    }

    @Test
    void internalCall() {
        callService.internal(); // internal active = true
    }

    /**
     * 프록시 방식의 AOP 한계
     * this.internal()로 this는 자기 객체를 가리키므로 실제 대상 객체 target 인스턴스를 뜻한다.
     * 결과적으로 이러한 내부호출은 프록시를 거치지 않는다. 따라서 트랜잭션을 적용할 수 없다.
     * target에 있는 internal()을 직접 호출해서 문제가 발생한 것이다.
     *
     * 해결 방법 : internal() 메서드를 별도의 클래스로 분리하는 것
     */
    @Test
    void externalCall() {
        callService.external(); // internal active = false
    }


    @TestConfiguration
    static class InternalCallV1Config {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }


    @Slf4j
    static class CallService {
        public void external() {
            log.info("call external");
            printTxInfo();
            internal(); // this.internal(); 실제 target에 있는 internal 호출, 즉 내부 호출
        }

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
