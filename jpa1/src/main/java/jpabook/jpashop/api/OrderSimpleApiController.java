package jpabook.jpashop.api;

import java.util.List;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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
     */
    @GetMapping("/api/simple-orders")
    public List<Order> orders() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); // 선택적으로 Lazy 강제 초기화
            order.getDelivery().getAddress(); // 선택적으로 Lazy 강제 초기화
            
        }

        return all;
    }
}
