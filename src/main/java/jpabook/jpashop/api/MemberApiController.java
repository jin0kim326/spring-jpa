package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
        return memberService.findMembers();
        /**
         * 1. 엔티티에있는 정보가 외부에 모두 노출되는문제 : @JsonIgnore를 사용해서 원하는것만 노출할 수 있겠지만 그것또한 문제
         * 다른 화면에서는 노출하고싶을수도있기때문, Valid와 마찬가지로 화면을 위한 로직이 들어온것이 때문에 좋지않은 설계
         *
         * 2. 엔티티의 일부를 변경하면 API스펙도 변경해버리는 문제
         * 3.
         */
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();

        List<MemberDto> data = findMembers.stream()
                .map(member -> new MemberDto(member.getName()))
                .collect(Collectors.toList());

        return new Result(data);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


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

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse editMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

     @Data
     static class CreateMemberRequest {
        private String name;
     }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
}
