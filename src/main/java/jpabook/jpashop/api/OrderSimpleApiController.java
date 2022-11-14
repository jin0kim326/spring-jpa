package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

/**
 * xToOne
 * Order
 * Order->Member
 * Order->Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy강제초기화 : order.getMember()까지는 프록시
            order.getDelivery().getOrder();
        }
        return all;

        /**
         * Order엔티티안의 Member 패치전략이 Lazy이기 때문에 멤버를 가져오지않음.
         * 그렇다고 null을 넣어둘 수는 없기때문에 하이버네이트는 프록시 멤버를만들어서 넣어둠
         * 스프링빈으로 하이버네이트모듈을 등록해두면됨
         * (단 실무에서 엔티티를 노출하지않기때문에 이런방법이있다는것만 인지할것)
         */
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        /**
         * ORDER 조회 : SQL 1
         * => 결과(ROW 2행)
         * 첫번째 주문에 멤버와 배송주소를 찾기위해 SQL각각1회 총2번 (지연로딩 조회)
         * 두번째 주문에 멤버와 배송주소를 찾기위해 sql각각1회 총2번 (지연로딩 조회)
         * => 총5회 (즉 성능이안좋음..)
         *
         * [[ N+1 문제 ]]
         * 1번째 쿼리(1)의 결과(N)만큼 쿼리가 더 나감
         * 즉 1 + 2 + 2 = 5가 나왔음, 참고로 이경우는 최악의경우이며 조회한 주문이 동일한 회원이 했다면
         * 1 + 1 + 2 = 4로 나옴 (영속성 컨텍스트에 해당 멤버가 있기때문에 멤버조회쿼리를 또 날리지는 않는다)
         */
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> OrderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        /**
         * 패치조인 적용
         * 쿼리 한번에 조회
         * 패치조인으로 order -> member, order->delivery는 이미 조회된 상태이므로 지연로딩X (객체 자체가 조회됨)
         * 실무에서 객체 그래프를 자주 사용하는것은 정해져있음
         */
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> OrderV4() {
        return orderRepository.findOrderDtos();
        /**
         * V4는 원하는데이터만 골라서 조회했다. 성능면에서 v3보다 더 개선되었다고 볼수있음
         * 그러나 재사용성이 낮음(화면에 fit 하게 맞춰서 조회하기때문에)
         * V3는 엔티티를 조회하기때문에 재사용성이 높음, 어플래이션 단에서 translate해서 데이터 조회가능
         *
         * API의 스펙에 맞춰서 개발되어있음 (화면이 변경되면 리포지토리를 수정해야함)
         */
    }

    /**
     * 정리
     * 1. 엔티티를 DTO로 변환하는 방법사용
     * 2. 필요하면 fetch join으로 성능최적화 -> 대부분 성능이슈 해결
     * 3. 그래도 안되면 DTO로 직접 조회하는 방법사용
     * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용
     */


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();//Lazy 초기화 : 영속성컨텍스트가 멤버아이디가지고 찾아서 없으면 쿼리날림
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
