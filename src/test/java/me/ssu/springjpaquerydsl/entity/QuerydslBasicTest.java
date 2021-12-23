package me.ssu.springjpaquerydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class QuerydslBasicTest extends BaseTest {

    @BeforeEach
    public void before() {
        // TODO field level에서 사용하기
        queryFactory = new JPAQueryFactory(entityManager);

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
    }

    // TODO JPQL
    @Test
    void startJPQL() {
        // TODO member1을 찾아라.
        // TODO Runtime 시점에 오류를 확인할 수 있음.
        String qlString = "select m from Member m where m.username=:username";

        Member findMember = entityManager
                .createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // TODO Querydsl
    @Test
    void startQuerydsl() {

        // TODO When, Querydsl(JPAQueryFactory)-1
        // TODO Field level로 빼기
//        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // TODO When, Querydsl(QHello)-2
        // TODO static member로 바로 적용가능(import)
//        QMember qMember = QMember.member;

        // TODO When, Querydsl(JPAQueryFactory + featchOne())-3
        Member findMember = queryFactory
                .select(member)
                .from(member)
                // TODO compile 시점에 오류 발견 가능
                .where(member.username.eq("member1"))
                .fetchOne();

        // TODO Then, Querydsl 검증
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
