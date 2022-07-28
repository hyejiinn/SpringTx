package com.example.springtransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 트랜잭션 전파의 기본 값은 REQUIRED
 * : 기존 트랜잭션이 없으면 새로운 트랜잭션을 만들고, 기존 트랜잭션이 있으면 참여한다.
 */
@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;
    @Autowired
    MemberService memberService;

    /**
     * 트랜잭션 전파 활용 1 : 서비스 계층에 트랜잭션이 없을 때 (커밋)
     * MemberService : @Transactional Off
     * MemberRepository : @Transactional On
     * LogRepository : @Transactional On
     */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then : 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * 트랜잭션 전파 활용 2 : 서비스 계층에 트랜잭션 없을때 (롤백)
     * MemberService : @Transactional Off
     * MemberRepository : @Transactional On
     * LogRepository : @Transactional On Exception
     * 회원 리포지토리는 정상 동작하지만, 로그 리포지토리에서 예외 발생
     */
    @Test
    void outerTxOff_fail() {
        // given
        String username = "로그예외_outerTxOff_fail";

        // when
        // logRepository는 해당 예외를 밖으로 던지고, 이 경우 트랜잭션 AOP가 예외를 받게 된다.
        // 런타임 예외가 발생해서 트랜잭션 AOP는 트랜잭션 매니저에 롤백 호출
        assertThatThrownBy(() -> memberService.joinV1(username)).isInstanceOf(RuntimeException.class);

        // then : 완전히 롤백되지 않고, member 데이터가 남아서 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * 트랜잭션 전파 활용 3 : 단일 트랜잭션
     * 회원 리포지토리와 로그 리포지토리를 하나의 트랜잭션으로 묶는 방법은 이 둘을 호출하는 회원 서비스에 트랜잭션을 사용하는 것이다.
     *
     * MemberService만 트랜잭션을 처리하기 때문에 논리 트랜잭션, 물리 트랜잭션, 외부 트랜잭션, 내부 트랜잭션, 트랜잭션 전파, rollbackOnly 등을 고려할 필요가 없다.
     */
    @Test
    void singleTx() {
        // given
        String username = "singleTx";

        // when
        memberService.joinV1(username);

        // then : 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }


}