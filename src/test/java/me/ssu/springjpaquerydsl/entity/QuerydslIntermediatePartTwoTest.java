package me.ssu.springjpaquerydsl.entity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

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
}
