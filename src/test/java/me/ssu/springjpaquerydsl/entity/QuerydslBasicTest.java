package me.ssu.springjpaquerydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    // TODO 검색 조건 쿼리(and)
    @Test
    void searchAnd() {
        Member findMember = queryFactory
                // TODO .select + .from == selectFrom
                .selectFrom(QMember.member)
                // TODO 이름이 멤버1이면서 나이가 10인 사람을 조회
                .where(member.username.eq("member1")
                        // TODO .and .or 가능
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // TODO 검색 조건 쿼리(between)
    @Test
    void searchBetween() {
        Member findMember = queryFactory
                // TODO .select + .from == selectFrom
                .selectFrom(QMember.member)
                // TODO 이름이 멤버1이면서 나이가 10인 사람을 조회
                .where(member.username.eq("member1")
                        // TODO between
                        .and(member.age.between(10, 30)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // TODO 검색 조건 쿼리(andParam)
    @Test
    void searchAndParam() {
        Member findMember = queryFactory
                // TODO .select + .from == selectFrom
                .selectFrom(QMember.member)
                // TODO 이름이 멤버1이면서 나이가 10인 사람을 조회
                // TODO andParam(, )
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // TODO 결과조회
    @Test
    void resultFetch() {
        // TODO fetch, 리스트 조회 데이터 없으면 빈 리스트 반환
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // TODO fetchOne, 단 건 조회
        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .fetchOne();

        // TODO
        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                // TODO fetchFirst == limit.fetchOne
//                .limit(1).fetchOne();
                .fetchFirst();

        // TODO fetchResults, 페이징 정보 포함, total count 쿼리 추가 실행
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        // TODO total Query
        results.getTotal();

        // TODO content Query
        List<Member> content = results.getResults();
    }
}