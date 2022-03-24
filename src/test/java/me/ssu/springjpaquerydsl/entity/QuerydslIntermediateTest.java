package me.ssu.springjpaquerydsl.entity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import me.ssu.springjpaquerydsl.dto.MemberDto;
import me.ssu.springjpaquerydsl.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;

public class QuerydslIntermediateTest extends BaseTest {

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

    /**
     * 프로젝션이 하나일 때
     */
    @Test
    void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 프로젝션이 두 개이상일 때
     *  반환 타입을 Tuple로 설정하기
     */
    @Test
    void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);

            System.out.println("username=" + username);
            System.out.println("age=" + age);
        }
    }

    /**
     * 순수 JPA에서 DTO 조회
     */
    @Test
    void findDtoJPQL() {
        // TODO "select m from Member m", MemberDto.class != 이부분은 Member Entity를 조회하는 거라
        //  둘의 타입이 맞지 않아 오류가 남.
        //  new 오퍼레이션 문법
        List<MemberDto> result = entityManager.createQuery(
                "select new me.ssu.springjpaquerydsl.dto.MemberDto(m.username, m.age) " +
                        "from Member m", MemberDto.class
        ).getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("" + memberDto);
        }
    }

    /**
     * 순수 JPA에서 DTO 조회(QueryDSL)
     *  Setter / Getter
     */
    @Test
    void findDtoBySetter() {
        // TODO bean(Getter / Setter)
        List<MemberDto> result = queryFactory.select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 순수 JPA에서 DTO 조회(QueryDSL)
     *  필드 직접 접근(private String username, Int age)
     *  private이지만 라이브러리에서 알아서 함.
     */
    @Test
    void findDtoByField() {
        // TODO filed()
        List<MemberDto> result = queryFactory.select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 순수 JPA에서 DTO 조회(QueryDSL)
     *  생성자 접근(private String username, Int age)
     */
    @Test
    void findDtoByConstructor() {
        // TODO constructor()
        List<MemberDto> result = queryFactory.select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 순수 JPA에서 DTO 조회(QueryDSL)
     *  별칭이 다를 때
     */
    @Test
    void findUserDto() {
        // TODO filed()
        List<UserDto> result = queryFactory.select(Projections.fields(UserDto.class,
                        // TODO 필드 명이 맞아야 되는데 여기서 MemberDto.class 값이다
//                        member.username,
//                        member.age))
                        // TODO .as로 처리하기
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }
    /**
     * 순수 JPA에서 DTO 조회(QueryDSL)
     *  별칭이 다를 때 여기서 또 서브쿼리 별칭을 처리할 때
     */
    @Test
    void findUserDtoSubQuery() {
        // TODO 서브쿼리 만들기
        QMember memberSub = new QMember("memberSub");

        // TODO filed()
        List<UserDto> result = queryFactory.select(Projections.fields(UserDto.class,
                        // TODO 필드 명이 맞아야 되는데 여기서 MemberDto.class 값이다
//                        member.username,
//                        member.age))
                        // TODO .as로 처리하기
                        member.username.as("name"),
                        // TODO SubQuery(age) : 최대값으로 나올 수 있게
                        //  ExpressionUtis와(... alias:"age"으로)
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                        )
                )
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * 순수 JPA에서 DTO 조회(QueryDSL)
     *  별칭이 다를 때 UserDto 생성자 접근
     */
    @Test
    void findUserDtoConstructor() {
        // TODO constructor()
        List<UserDto> result = queryFactory.select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }
}
