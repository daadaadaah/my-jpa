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
