package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
