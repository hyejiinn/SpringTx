package com.example.springtransaction.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    /**
     * 회원과 DB로그를 함께 남기는 비지니스 로직이다.
     * @param username
     */
    @Transactional
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("== memberRepository 호출 시작 == ");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 == ");

        log.info("== logRepository 호출 시작 == ");
        logRepository.save(logMessage);
        log.info("== logRepository 호출 종료 == ");
    }

    /**
     * DB로그 저장시 예외가 발생하면 예외를 복구한다.
     * @param username
     */
    @Transactional
    public void joinV2(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("== memberRepository 호출 시작 == ");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 == ");

        log.info("== logRepository 호출 시작 == ");
        try {
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("log 저장에 실패했습니다. logMessage={}", logMessage.getMessage());
            log.info("정상 흐름 변환");
        }
        log.info("== logRepository 호출 종료 == ");
    }

}
