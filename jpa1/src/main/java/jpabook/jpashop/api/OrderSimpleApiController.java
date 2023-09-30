package jpabook.jpashop.api;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * [문제]
     * - 무한 루프에 빠짐(https://github.com/daadaadaah/my-jpa/issues/3)
     *
     * [해결 1]
     * 1. 양방향이 걸린 부분 중 한 쪽을 @JsonIgnore 걸어 준다.
     * 2. Hibernate5Module 모듈 등록, 강제 지연 로딩 true로 설정
     *
     * [해결 2]
     * 1. 양방향이 걸린 부분 중 한 쪽을 @JsonIgnore 걸어 준다.
     * 2. Hibernate5Module 모듈 등록, 선택적으로 LAZY 로딩 강제 초기화
     *
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * [문제]
     * - 지연로딩으로 1 + N  문제(= 1(orders) + N((회원 N + 배송 N) * 2)) 발생함 -> V1, V2 둘다
     * - 지연로딩의 경우, 맨처음에는 영속성 컨텍스트에서 데이터 있는지 확인한 후 없으면 DB 쿼리 날림
     *
     * [해결]
     * - fetch join 사용 -> V3 참고
     *
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     *
     * [fetch join 적용 전] : 5개의 쿼리 날라감
     * 1. Order 테이블 조회 쿼리
     *     select
     *         order0_.order_id as order_id1_6_,
     *         order0_.delivery_id as delivery4_6_,
     *         order0_.member_id as member_i5_6_,
     *         order0_.order_date as order_da2_6_,
     *         order0_.status as status3_6_
     *     from
     *         orders order0_
     *     inner join
     *         member member1_
     *             on order0_.member_id=member1_.member_id limit ?
     *
     * 2. Member 테이블 조회 쿼리
     *     select
     *         member0_.member_id as member_i1_4_0_,
     *         member0_.city as city2_4_0_,
     *         member0_.street as street3_4_0_,
     *         member0_.zipcode as zipcode4_4_0_,
     *         member0_.name as name5_4_0_
     *     from
     *         member member0_
     *     where
     *         member0_.member_id=?
     *
     * 3. Delivery 테이블 조회 쿼리
     *     select
     *         delivery0_.delivery_id as delivery1_2_0_,
     *         delivery0_.city as city2_2_0_,
     *         delivery0_.street as street3_2_0_,
     *         delivery0_.zipcode as zipcode4_2_0_,
     *         delivery0_.status as status5_2_0_
     *     from
     *         delivery delivery0_
     *     where
     *         delivery0_.delivery_id=?
     *
     * 4. Member 테이블 조회 쿼리
     *     select
     *         member0_.member_id as member_i1_4_0_,
     *         member0_.city as city2_4_0_,
     *         member0_.street as street3_4_0_,
     *         member0_.zipcode as zipcode4_4_0_,
     *         member0_.name as name5_4_0_
     *     from
     *         member member0_
     *     where
     *         member0_.member_id=?
     *
     * 5. Delivery 테이블 조회 쿼리
     *     select
     *         delivery0_.delivery_id as delivery1_2_0_,
     *         delivery0_.city as city2_2_0_,
     *         delivery0_.street as street3_2_0_,
     *         delivery0_.zipcode as zipcode4_2_0_,
     *         delivery0_.status as status5_2_0_
     *     from
     *         delivery delivery0_
     *     where
     *         delivery0_.delivery_id=?
     *
     * [fetch join 적용 후] : 1개 쿼리 날라감
     *     select
     *         order0_.order_id as order_id1_6_0_,
     *         member1_.member_id as member_i1_4_1_,
     *         delivery2_.delivery_id as delivery1_2_2_,
     *         order0_.delivery_id as delivery4_6_0_,
     *         order0_.member_id as member_i5_6_0_,
     *         order0_.order_date as order_da2_6_0_,
     *         order0_.status as status3_6_0_,
     *         member1_.city as city2_4_1_,
     *         member1_.street as street3_4_1_,
     *         member1_.zipcode as zipcode4_4_1_,
     *         member1_.name as name5_4_1_,
     *         delivery2_.city as city2_2_2_,
     *         delivery2_.street as street3_2_2_,
     *         delivery2_.zipcode as zipcode4_2_2_,
     *         delivery2_.status as status5_2_2_
     *     from
     *         orders order0_
     *     inner join
     *         member member1_
     *             on order0_.member_id=member1_.member_id
     *     inner join
     *         delivery delivery2_
     *             on order0_.delivery_id=delivery2_.delivery_id
     */
    @GetMapping("/api/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
