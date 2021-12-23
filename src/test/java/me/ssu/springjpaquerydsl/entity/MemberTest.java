package me.ssu.springjpaquerydsl.entity;

import me.ssu.springjpaquerydsl.common.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

class MemberTest extends BaseTest {

    @Test
    void testEntity() {
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

        // TODO 초기화, flush : 영속성 컨텍스트에 있는 오프젝트를 실제 쿼리를 만들어 DB로 날
        entityManager.flush();
        // TODO 초기화, clear : 영속성 컨텍스트 완전히 초기화해 캐쉬가 날라감.
        entityManager.clear();

        List<Member> members = entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();

        // TODO sout 찍기
        for (Member member : members) {
            System.out.println("member" + member);
            System.out.println("-> member.team" + member.getTeam());
        }
    }
}
