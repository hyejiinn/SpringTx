package com.example.springtransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

/**
 * 트랜잭션 전파 (기본 REQUIRED)
 * 트랜잭션이 이미 진행중인데 여기에 추가로 트랜잭션을 수행할때 어떻게 동작할지 결정하는 것
 * 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
 * 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.
 */
@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute()); // 트랜잭션 매니저를 통해 트랜잭션 획득
        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());  // 트랜잭션 매니저를 통해 트랜잭션 획득
        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    /**
     * 트랜잭션 두번 사용
     * -> 트랜잭션1 완전히 끝나면 트랜잭션2 수행
     * 트랜잭션이 각각 수행되면서 사용되는 DB 커넥션도 각각 다르다.
     */
    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus transaction1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(transaction1);

        log.info("트랜잭션2 시작");
        TransactionStatus transaction2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.commit(transaction2);
    }

    /**
     * 트랜잭션1은 커밋, 트랜잭션2는 롤백
     */
    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus transaction1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        txManager.commit(transaction1);

        log.info("트랜잭션2 시작");
        TransactionStatus transaction2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        txManager.rollback(transaction2);
    }

    /**
     * 외부 트랜잭션 수행 중 내부 트랜잭션을 추가로 수행
     * 내부 트랜잭션을 시작하는 시점에 이미 외부 트랜잭션이 진행중인 상태로 이 경우에는 내부 트랜잭션이 외부 트랜잭션에 참여한다.
     * 참여한다 = 내부 트랜잭션이 외부 트랜잭션을 그대로 이어 받아서 따른다는 것으로 내부 트랜잭션과 외부 트랜잭션이 하나의 물리 트랜잭션으로 묶이는 것이다.
     *
     * 트랜잭션 매니저를 통해 논리 트랜잭션을 관리하고, 모든 논리 트랜잭션이 커밋되면 물리 트랜잭션이 커밋된다.
     */
    @Test
    void inner_commit() {
        // 처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리한다.
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}", outer.isNewTransaction()); // true 신규 트랜잭션인 경우에만 물릴 커밋과 롤백 수행

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction={}", inner.isNewTransaction()); // false
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner); // Participating in existing transaction 아직 트랜잭션 끝난 것이 아니기 때문에 실제 커밋 호출 X

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); // Initiating transaction commit
    }

    /**
     * 스프링 트랜잭션 전파 - 외부 롤백
     * 내부 트랜잭션은 커밋되었는데 외부 트랜잭션이 롤백되는 상황
     *
     * 외부 트랜잭션에서 시작한 물리 트랜잭션의 범위가 내부 트랜잭션까지 사용된다.
     * 이후 외부 트랜잭션이 롤백되면서 전체 내용이 롤백된다.
     */
    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}", outer.isNewTransaction()); // true

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction={}", inner.isNewTransaction()); // false
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        // 외부 트랜잭션은 신규 트랜잭션이기 때문에 DB 커넥션에 실제 롤백을 호출한다.
        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);
    }

    /**
     * 스프링 트랜잭션 전파 - 내부 롤백
     * 내부 트랜잭션은 롤백되었는데 외부 트랜잭션이 커밋되는 상황
     *
     * 논리 트랜잭션이 하나라도 롤백되면 물리 트랜잭션은 롤백된다.
     * 외부 트랜잭션을 커밋할 때 rollbackOnly 를 확인하고, 마크 표시가 되어 있으므로 물리 트랜잭션을 롤백하고,
     * UnexpectedRollbackException 예외를 던진다.
     */
    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}", outer.isNewTransaction()); // true

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction={}", inner.isNewTransaction()); // false

        // Participating transaction failed - marking existing transaction as rollback-only
        // 내부 트랜잭션을 롤백하면 실제 물리 트랜잭션을 롤백하지는 않고 대신 기존 트랜잭션을 롤백 전용으로 표시한다.
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);

        // Global transaction is marked as rollback-only but transactional code requested commit
        // Initiating transaction rollback
        // 커밋을 호출했지만 전체 트랜잭션이 롤백 전용으로 표시되었으므로 물리 트랜잭션을 롤백한다.
        log.info("외부 트랜잭션 커밋");

        // 개발자 입장에서는 분명 커밋을 시도했는데, 기대하지 않은 롤백이 발생했다는 것을 명확하게 알려준다.
        Assertions.assertThatThrownBy(() -> txManager.commit(outer)).isInstanceOf(UnexpectedRollbackException.class);

    }



}
