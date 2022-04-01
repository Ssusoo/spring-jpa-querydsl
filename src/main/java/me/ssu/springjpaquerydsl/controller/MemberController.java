package me.ssu.springjpaquerydsl.controller;

import lombok.RequiredArgsConstructor;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.repository.MemberRealJpaRepository;
import me.ssu.springjpaquerydsl.repository.MemberSpringJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRealJpaRepository memberJpaRepository;
    private final MemberSpringJpaRepository memberSpringJpaRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        // TODO Where절에 파라미터를 사용한 예제
        return memberJpaRepository.search(condition);
    }

    // TODO 스프링 데이터 JPA에서 제공하는 페이징 처리
    //  전체 카운트를 한번에 조회하는 단순한 방법
    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberSpringJpaRepository.searchPageSimple(condition, pageable);
    }

    // TODO QueryDSLCount 쿼리 최적화
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberSpringJpaRepository.searchPagePerformanceOptimization(condition, pageable);
    }
}
