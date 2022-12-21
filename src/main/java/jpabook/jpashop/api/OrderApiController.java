package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /**
     * DTO를 조회 (조회성능 낮음)
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * 패치조인 적용 (Distinct)
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        /* JPQL */
        List<Order> orders= orderRepository.findAllWithItem();

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }
    /**
     * 페치 조인을 적용함으로써 SQL이 1번만 실행
     *
     * *단점: 페이징 쿼리가 불가
     * 1대다 조인을 하는순간 Order의 기준이 달라짐(개발자는 2개가 나올것을 예상하지만 디비결과는 4개)
     * 하이버네이트는 경고를 내고, 모든 데이터를 디비에서 읽어오고 메모리에서 페이징을 함(매우 위험)
     *
     * + 컬렉션 패치 조인은 1개만 사용가능
     *   컬렉션 둘 이상에 페치조인을 사용하면 안됨 (데이터가 부정확하게 조회될수있음)
     */



    /**
     * 🔥페이징 한계돌파🔥
     * - ToOne 관계는 페치조인으로 한번에 가져온다
     * - 컬렉션은 지연 로딩으로 조회한다.
     * - 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size 를 적용
     *   이 옵션 사용시 컬렉션,프록시 객체를 한꺼번에 설정한 size 만큼 IN쿼리로 조회
     *
     *   => 쿼리 호출 수가 1+N => 1+1로 최적화
     *   => 조인보다 DB데이터 전송량이 최적화 (중복데이터 최소화됨)
     *   => 페이징 가능
     *
     *   결론 : ToOne 관계는 페치조인(쿼리수 감소) + 컬렉션은 default_batch_fetch_size 사용
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders= orderRepository.findAllWithMemberDelivery(offset, limit);    //ToOne관계인것들을 모두 fetch조인으로 가져옴

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * 플랫데이터 장점
     * => 쿼리가 한번만 나감
     *
     * 단점
     * - 페이징 불가 (Order기준으로 해야하는데 OrderItems가 기준이됨)
     * 🔥 API 스펙이 맞지 않음. V5와 같은 스펙으로 반환하기 위해서는 작업이 필요함..
     * 🔥 이 작업이 복잡...
     *
     */
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> orderV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        // OrderFlatDto --> OrderQueryDto를 개발자가 직접 변환하기..

        return flats;
    }

    /**
     * JPA로 API 개발 권장 순서
     * 1.엔티티 조회 방식
     * 2. 컬렉션 최적화
     *    -> 페이징필요 batch_fetch_size, @BatchSize 로 최적화
     *    -> 페이징 필요X -> 페치 조인 사용
     * 3. 엔티티 조회 방식으로 해결이 안되면 DTO조회
     * 4. DTO조회 방식으로 안된다 -> NativeSQL or 스프링 JdbcTemplate
     */

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();
//            o.getOrderItems().stream().forEach(oi->oi.getItem().getName());
//            orderItems = o.getOrderItems();
            orderItems = o.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getOrderPrice();
        }
    }
}
