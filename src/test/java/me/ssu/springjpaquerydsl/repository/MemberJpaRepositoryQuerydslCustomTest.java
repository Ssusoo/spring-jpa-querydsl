package me.ssu.springjpaquerydsl.repository;

import me.ssu.springjpaquerydsl.common.JpaBaseTest;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.entity.Member;
import me.ssu.springjpaquerydsl.entity.Team;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberJpaRepositoryQuerydslCustomTest extends JpaBaseTest {

    /**
     *  동적 쿼리와 성능 테스트 Test
     *      동적 쿼리 Where 적용
     */
    @Test
    void searchWhereTest() {
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

        // TODO 조건 전부(동적쿼리)
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        // TODO 동적쿼리와 성능 최적화(동적쿼리 Where절)
        List<MemberTeamDto> result = memberSpringJpaRepository.search(condition);

        // TODO 조건 전부
        assertThat(result).extracting("username").containsExactly("member4");
    }
}
