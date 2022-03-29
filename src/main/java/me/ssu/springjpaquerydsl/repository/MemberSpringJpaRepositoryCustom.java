package me.ssu.springjpaquerydsl.repository;


import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// TODO 사용자 정의 인터페이스 작성-1
// TODO 스프링 데이터 JPA에서 제공하는 페이징 처리(Page, Pageable)-2
public interface MemberSpringJpaRepositoryCustom {
    // TODO 사용자 정의 인터페이스 작성-1
    List<MemberTeamDto> search(MemberSearchCondition condition);

    // TODO 스프링 데이터 JPA에서 제공하는 페이징 처리(Page, Pageable)-2-1
    //  전체 카운트를 한번에 조회하는 단순한 방법
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    // TODO 스프링 데이터 JPA에서 제공하는 페이징 처리(Page, Pageable)-2-2
    //  데이터 내용과 전체 카운트를 별도로 조회하는 방법
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
