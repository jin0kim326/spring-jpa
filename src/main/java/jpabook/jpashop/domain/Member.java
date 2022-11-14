package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int age;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") // Order에 있는 member 필드에 의해 매핑되었음. -> Order의 member가 연관관계의 주인
    private List<Order> orders = new ArrayList<>();
}
