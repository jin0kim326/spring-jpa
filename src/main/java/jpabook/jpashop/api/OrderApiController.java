package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

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
    }

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
