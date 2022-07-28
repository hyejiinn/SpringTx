package com.example.springtransaction.order;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "orders") // 데이터베이스 예약어 order by가 있어서 orders로 테이블명을 지정해준다.
@Getter
@Setter // 실무에서 엔티티에 @Setter를 사용해 불필요한 변경 포인트를 주는 것은 좋지 않다.
public class Order {

    @Id @GeneratedValue
    private Long id;

    private String username; // 정상, 예외, 잔고부족
    private String payStatus;  // 대기, 완료

    public Order() {
    }
}
