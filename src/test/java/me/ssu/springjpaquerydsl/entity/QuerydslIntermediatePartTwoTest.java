package me.ssu.springjpaquerydsl.entity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

public class QuerydslIntermediatePartTwoTest extends BaseTest {

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
     * 동적 쿼리 해결 BooleanBuilder
     */
    @Test
    void 동적쿼리_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();

        if (usernameCond != null) {
            // TODO AND 조건 넣기
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null ) {
            // TODO AND 조건 넣기
            builder.and(member.age.eq(ageCond));
        }

        // TODO 여기서 Main 쿼리가 이 부분에 나오기 때문에
        //  직관적이지 않음.
        return queryFactory
                .selectFrom(member)
                // TODO 둘 다 값이 있거나 둘 중 하나만 있거나 등 결과를 builder
                .where(builder)
                .fetch();
    }

    /**
     * 동적 쿼리 해결 Where 다중 파라미터 사용
     */
    @Test
    void 동적쿼리_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    // TODO 개발 할 때 이걸 보지만(동적 쿼리를 하나 보내 머리속으로 정리가 됨.)
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        // TODO BooleanBuilder에 비해 직관적임.
        return queryFactory.selectFrom(member)
                // TODO Predicate 각각 설정할 때
//                .where(usernameEq(usernameCond), ageEq(ageCond))
                // TODO BooleanExpression 두 개를 같이
                //  장점은 조립을 할 수 있음.
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    // TODO 아래 조건을 안 봄-1
    //  Predicate -> BooleanExpression
//    private Predicate usernameEq(String usernameCond) {
    private BooleanExpression usernameEq(String usernameCond) {
        // TODO null이 반환 되면 where null이 무시 됨.
        //  아무 역할을 하지 않기 때문에 동적 쿼리가 만들어지는 거임.
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }
    // TODO 아래 조건을 안 봄-1
    //  Predicate -> BooleanExpression
//    private Predicate ageEq(Integer ageCond) {
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // TODO 조합 가능
    // TODO 위에 두 개를 조합해서 쓸 때는 위에 둘을 Predicate -> BooleanExpression로 수정하기
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    /**
     * 수정, 삭제 벌크 연산
     *  멤버의 나이가 28살 미만이면 비회원으로 이름을 바꾼다.
     *
     * 벌크 연산의 단점
     *  영속성 컨텍스트의 DB 상태와 벌크 연산의 DB 상태가 다름
     *  벌크 연산은 영속성 컨텍스트를 무시한 채 DB의 데이터를 바꿔버림림
     */
    // TODO @Transaction 시키기면 Rollback하기 때문에
    @Test
    @Commit
    void bulkUpdate() {
        // TODO 벌크 연산 실행되기 전
        //  member1 = 10 -> DB member1
        //  member2 = 20 -> DB member1
        //  member3 = 30 -> DB member3
        //  member4 = 40 -> DB member4

        // TODO count는 영향을 받은 row 수가 나옴.
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))   // lt age < 28
                .execute();

        // TODO 벌크 연산 실행 후(영속성 컨텍스트를 무시한 채 DB를 바꿔버림)
        //  id=1 member1 = 10 -> id=1 DB 비회원
        //  id=2 member2 = 20 -> id=2 DB 비회원
        //  id=3 member3 = 30 -> id=3 DB member3
        //  id=4 member4 = 40 -> id=4 DB member4

        // TODO JPA 영속성 컨텍스트와 벌크연산 해결 방법
        entityManager.flush(); // 영속성 컨텍스트의 변경 내용을 DB에 저장
        entityManager.clear(); // 영속성 컨텍스트의 값을 초기화

        // TODO SQL이 실행되고 DB에서는 값을 가져온다.
        //  JPA DB의 결과를 영속성 컨텍스트에 넣어 줘야하는데
        //  영속성 컨텍스트에 member1의 중복이 있으면 DB의 값을 버리고
        //  순위는 영속성 컨텍스트가 우선권을 가짐.
        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }
}
