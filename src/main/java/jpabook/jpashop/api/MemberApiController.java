package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        // Json데이터를 Member엔티티에 넣어줌
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
        /**
         * 1.화면(프레젠테이션 계층)의 벨리데이션이 엔티티에 있는것은 좋지않음.
         * 2.엔티티의 spec을 바꾸면 API 스펙이 바뀜 -> 요청 스펙에 맞게 별도의 DTO를 받는것이 좋음
         */
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
        /**
         * V1에 비해 코드가 약간 복잡해 지긴 했지만
         * 엔티티의 스펙이 변경되어도 API에는 영향을 주지 않음
         */
    }

     @Data
     static class CreateMemberRequest {
        private String name;
     }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }
}
