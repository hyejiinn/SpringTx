package com.example.springtransaction.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * 예외와 트랜잭션 커밋, 롤백
 * 언체크 예외인 RuntimeException, Error 와 같은 그 하위 예외가 발생하면 트랜잭션을 롤백한다.
 * 체크 예외인 Exception과 그 하위 예외가 발생하면 트랜잭션을 커밋한다.
 */
@Slf4j
@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;

    @Test
    void runtimeException() {
        // Initiating transaction rollback
        assertThatThrownBy(() -> rollbackService.runtimeException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkedException() {
        // Initiating transaction commit
        assertThatThrownBy(() -> rollbackService.checkedException()).isInstanceOf(MyException.class);
    }

    @Test
    void rollbackFor() {
        // Initiating transaction rollback
        assertThatThrownBy(() -> rollbackService.rollbackFor()).isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class RollbackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    @Slf4j
    static class RollbackService {

        // 런타임 예외 : 롤백
        @Transactional
        public void runtimeException() {
            log.info("call RuntimeException");
            throw new RuntimeException();
        }

        // 체크 예외 : 커밋
        @Transactional
        public void checkedException() throws MyException {
            log.info("call CheckedException");
            throw new MyException();
        }

        // 체크 예외 rollbackFor 지정 : 롤백
        @Transactional(rollbackFor = MyException.class) // 어떤 예외가 발생할 때 롤백할지 지정
        public void rollbackFor() throws MyException {
            log.info("call rollbackFor");
            throw new MyException();
        }
    }


    static class MyException extends Exception {
    }


}
