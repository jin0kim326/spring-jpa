package jpabook.jpashop.service;

import jpabook.jpashop.domain.Trader;
import jpabook.jpashop.domain.Transaction;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@SpringBootTest
@Transactional
public class TransactionTest {
    @Autowired
    EntityManager em;
    @Autowired OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void test() throws Exception {
        // given
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario", "Milan");
        Trader alan = new Trader("Alan", "Cambridge");
        Trader brian = new Trader("Brian", "Cambridge");

        List<Transaction> transactions = Arrays.asList(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );

        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }

        //1. 2011년에 일어난 모든 트랜잭션을 찾아 오름차순 정리
        List<Transaction> one = transactions.stream()
                .filter(ts -> ts.getYear() == 2011)
                .collect(toList());

        for (Transaction transaction : one) {
            System.out.println(transaction);
        }

        System.out.println("=======[2] 거래자가 근무하는 모든 도시를 중복 없이 나열 =======");
        transactions.stream()
                .map(ts -> ts.getTrader().getCity())
                .distinct()
                .forEach(System.out::println);

        System.out.println("=======[3] 케임브리지에서 근무하는 모든 거래자를 찾아서 이름순 정렬 =======");
        transactions.stream()
                .filter(ts-> ts.getTrader().getCity().equals("Cambridge"))
                .sorted(Comparator.comparing(ts-> ts.getTrader().getName()))
                .forEach(System.out::println);

        System.out.println("=======[4] 모든 거래자의 이름을 알파벳순으로 정렬 =======");

        transactions.stream()
                .map(ts -> ts.getTrader().getName())
                .distinct()
                .sorted()
                .forEach(System.out::println);

        String tradeStr = transactions.stream()
                .map(ts -> ts.getTrader().getName())
                .distinct()
                .sorted()
                .reduce("", (n1, n2) -> n1 + n2);

        String tradeStr2 = transactions.stream()
                .map(ts -> ts.getTrader().getName())
                .distinct()
                .sorted()
                .collect(joining());
        System.out.println(tradeStr + ":::" + tradeStr2);

        System.out.println("=======[5] 밀라노에 거래자가 있는가? =======");
        boolean milan = transactions.stream()
                .anyMatch(ts -> ts.getTrader().getCity().equals("Milan"));
        System.out.println(milan);

        System.out.println("=======[6] 케임브리지에서 거주하는 거래자의 모든 트랜잭션값을 출력하시오. =======");
        transactions.stream()
                .filter(ts -> ts.getTrader().getCity().equals("Cambridge"))
                        .map(Transaction::getValue)
                                .forEach(System.out::println);

        System.out.println("=======[7] 전체 트랜잭션 중 최댓값은 얼마? =======");
        transactions.stream()
                .map(Transaction::getValue)
                .reduce(Integer::max)
                        .ifPresent(System.out::println);

        transactions.stream()
                        .min(Comparator.comparing(Transaction::getValue))
                                .ifPresent(System.out::println);

        System.out.println("=======[8] 전체 트랜잭션 중 최솟값은 얼마? =======");
        transactions.stream()
                .map(Transaction::getValue)
                .reduce(Integer::min)
                        .ifPresent(System.out::println);
    }
}
