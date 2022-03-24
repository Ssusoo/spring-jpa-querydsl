package me.ssu.springjpaquerydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static me.ssu.springjpaquerydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

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
//        // TODO fetch, 리스트 조회 데이터 없으면 빈 리스트 반환
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        // TODO fetchOne, 단 건 조회쓰기
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        // TODO
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                // TODO fetchFirst == limit.fetchOne
////                .limit(1).fetchOne();
//                .fetchFirst();

        // TODO fetchResults, 페이징 정보 포함, total count 쿼리 추가 실행
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        // TODO total Query
        results.getTotal();

        // TODO content Query
        List<Member> content = results.getResults();
    }

    // TODO 정렬 조회

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    void sort() {
        // TODO Given
        entityManager.persist(new Member(null, 100));
        entityManager.persist(new Member("member5", 100));
        entityManager.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(
                        member.age.desc(),
                        member.username.asc()
                                .nullsLast()
                )
                .fetch();

        // TODO When
        Member member5 = result.get(0);     // 회원이름 올리차순
        Member member6 = result.get(1);     // 회원이름 올리차순
        Member memberNull = result.get(2);  // 회원 이름이 없으면 마지막에 출력

        // TODO Then, 검증
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    // TODO 페이징(OrderBy를 넣어야 함)
    // TODO 조회 건수 제한-1
    @Test
    void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)      // 0부터 시작이라 1(하나)를 스킵
                .limit(2)       // 최대 2건 조회
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    // TODO 전체 조회수-2
    @Test
    void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)      // 0부터 시작이라 1(하나)를 스킵
                .limit(2)       // 최대 2건 조회
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이 * from Member m
     */
    // TODO 집합
    @Test
    void aggregation() {
        List<Tuple> results = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = results.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100); // 10/20/30/40
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    // TODO GroupBy
    @Test
    void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);  // 10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);  // 30 + 40) / 2
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    void theta_join() {
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인.
     * 회원은 모두 조회해라
     * JPQL : SELECT m, t FROM member m LEFT JOIN m.team t ON t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id AND t.name='teamA'
     */
    @Test
    void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
//                .leftJoin(member.team, team)
//                .on(team.name.eq("teamA"))
                .where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t ON m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    void join_on_no_relation() {
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                // TODO id 값으로 하는 조인이 아니라 이름으로 필터링됨.
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 페치 조인 미적용
     * 지연로딩으로 Member, Team SQL 쿼리 각각 실행
     *
     * @PersistenceUnit 테스트 증명(로딩 된 Entity인지 초기화된 Entity인지
     * isLoaded()를 통해 알려줌
     */
    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Test
    void fetchJoinNo() throws Exception {
        // TODO 영속성 컨텍스트 날리고 시작
        entityManager.flush();
        entityManager.clear();

        // TODO Team(fetch = FetchType.LAZY 세팅이기 때문에 현재 DB에서 조회할 때 Member만 조회가 됨)
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                // TODO fetchOne(단 건 조회)
                .fetchOne();

        // TODO True/False 검증하기
        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    /**
     * 페치 조인 적용
     * 즉시 로딩으로 Member, Team SQL
     * 쿼리 조인으로 한 번에 조회
     *
     * @throws Exception
     */
    @Test
    void fetchJoinUse() throws Exception {
        // TODO 영속성 컨텍스트 날리고 시작
        entityManager.flush();
        entityManager.clear();

        // TODO Team(fetch = FetchType.LAZY 세팅이기 때문에 현재 DB에서 조회할 때 Member만 조회가 됨)
        Member findMember = queryFactory
                .selectFrom(member)
                // TODO fetchJoin
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                // TODO fetchOne(단 건 조회)
                .fetchOne();

        // TODO True/False 검증하기
        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 서브쿼리
     * eq 사용
     * 나이가 가장 많은 회원조회
     */
    @Test
    void subQuery() throws Exception {
        // TODO 서브쿼리이기 때문에 밖에 member랑 별칭(Alias)이 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        // TODO 서브쿼리(JPAExpressions 사용)
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        // TODO 두 객체를 비교할 때 assertThat()
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 서브쿼리
     * goe (>=) 사용
     * 나이가 평균 이상인 회원조회
     */
    @Test
    void subQueryGoe() throws Exception {
        // TODO 서브쿼리이기 때문에 밖에 member랑 별칭(Alias)이 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        // TODO 서브쿼리(JPAExpressions 사용)
                        JPAExpressions
                                .select(memberSub.age.avg())    // avg : 평균
                                .from(memberSub)
                ))
                .fetch();

        // TODO 두 객체를 비교할 때 assertThat()
        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 서브쿼리 여러 건 처리 in 사용
     */
    @Test
    void subQueryIn() throws Exception {
        // TODO 서브쿼리이기 때문에 밖에 member랑 별칭(Alias)이 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        // TODO 서브쿼리(JPAExpressions 사용)
                        JPAExpressions
                                .select(memberSub.age)    // avg : 평균
                                .from(memberSub)
                                .where(memberSub.age.gt(10)) // age > 10
                ))
                .fetch();

        // TODO 두 객체를 비교할 때 assertThat()
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    /**
     * select 서브쿼리
     */
    @Test
    void subQuerySelect() throws Exception {
        // TODO 서브쿼리이기 때문에 밖에 member랑 별칭(Alias)이 겹치면 안 됨.
        QMember memberSub = new QMember("memberSub");

        List<Tuple> fetch = queryFactory
                // TODO Select 서브쿼리
                .select(
                        member.username, JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        // TODO 회원 정보 출력
        for (Tuple tuple : fetch) {
            // TODO 유저 이름 출력
            System.out.println("username = " + tuple.get(member.username));
            // TODO 유저 이름의 평균 나이
            System.out.println("age" +
                tuple.get(
                    JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub))
            );
        }
    }
}




