package me.ssu.springjpaquerydsl.repository;


import me.ssu.springjpaquerydsl.common.JpaBaseTest;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.entity.Member;
import me.ssu.springjpaquerydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberSpringJpaPageableSimpleRepositoryTest extends JpaBaseTest {

    /**
     * 스프링 데이터 JPA에서 제공하는 페이징 처리 Test
     *  전체 카운트를 한번에 조회하는 단순한 방법
     */
    @Test
    void searchPageTest() {
        // TODO Team 객체 생성
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        // TODO DB에 넣기
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        // TODO Member 객체 생성
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        // TODO DB에 넣기
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        // TODO 검색 조건
        MemberSearchCondition condition = new MemberSearchCondition();

        // TODO 스프링 데이터 JPA는 0번째 인덱스부터 시작
        //  0 번째 페이지에서 3개를 보여준다.
        PageRequest pageRequest = PageRequest.of(0, 3);

        // TODO 전체 카운트를 한번에 조회하는 단순한 방법
        Page<MemberTeamDto> result = memberSpringJpaRepository.searchPageSimple(condition, pageRequest);

        // TODO 페이지에서 보여주는 갯수 검증
        assertThat(result.getSize()).isEqualTo(3);
        // TODO Content 검증
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}
