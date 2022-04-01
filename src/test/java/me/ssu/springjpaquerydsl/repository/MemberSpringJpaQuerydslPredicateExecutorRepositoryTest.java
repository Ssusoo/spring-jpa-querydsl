package me.ssu.springjpaquerydsl.repository;


import me.ssu.springjpaquerydsl.common.JpaBaseTest;
import me.ssu.springjpaquerydsl.entity.Member;
import me.ssu.springjpaquerydsl.entity.QMember;
import me.ssu.springjpaquerydsl.entity.Team;
import org.junit.jupiter.api.Test;

public class MemberSpringJpaQuerydslPredicateExecutorRepositoryTest extends JpaBaseTest {

    @Test
    void querydslPredicateExecutorTest() {
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

        QMember member = QMember.member;
        Iterable<Member> result = memberSpringJpaRepository.findAll(
                member.age.between(20, 40)
                        .and(member.username.eq("member1")));
        for (Member findMember : result) {
            System.out.println("member1" + findMember);
        }
    }
}
