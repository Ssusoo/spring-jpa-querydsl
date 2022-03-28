package me.ssu.springjpaquerydsl.repository;


import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;

import java.util.List;

// TODO 사용자 정의 인터페이스 작성
public interface MemberSpringJpaRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
