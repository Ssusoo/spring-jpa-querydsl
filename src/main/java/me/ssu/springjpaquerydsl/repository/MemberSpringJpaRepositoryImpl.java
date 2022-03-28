package me.ssu.springjpaquerydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static me.ssu.springjpaquerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

// TODO 내가 정의한 인터페이스를 받는다
//  스프링 데이터 JPA 인터페이스 클래스 + Impl 이름 붙이기
public class MemberSpringJpaRepositoryImpl implements MemberSpringJpaRepositoryCustom {

    // TODO QueryDSL을 사용하기 위해 JPAQueryFactory 필요하다.
    private final JPAQueryFactory queryFactory;

    // TODO QueryDSL과 EntityManager Injection
    public MemberSpringJpaRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    // TODO Where절에 파라미터를 사용한 예제
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        // TODO 빌더를 통해 동적쿼리 처리하기-1
        return queryFactory
                .select(new QMemberTeamDto(
                        // TODO 멤버는 필드명의 아이디이기 때문에 as()
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName"))
                )
                .from(member)
                // TODO Member와 Team Join하기(Team의 데이터를 다 가져오기 때문에
                .leftJoin(member.team, team)
                // TODO Where절에 파라미터(동적쿼리)-1
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    // TODO Where절에 파라미터(동적쿼리)-2
    //  Predicate -> BooleanExpression(import QueryDSL)
    //  BooleanExpression으로 하면 AND OR BetWeen 조합도 가능하하다(재사용도 가능하다)
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }


    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe!= null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
