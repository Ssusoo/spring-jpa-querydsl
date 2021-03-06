package me.ssu.springjpaquerydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.dto.QMemberTeamDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static me.ssu.springjpaquerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

/**
 *  커스텀한 QueryDSL이 아니라 조회 전용.
 */
// TODO Search가 API나 어떤 화면에 특화된 기능이라면
//  커스텀하게 만드는 게 아니라 조회 전용으로 따로 빼주는 것도 하나의 방법임.
@Repository
public class MemberSpecialQuerydslRepository {
    // TODO QueryDSL을 사용하기 위해 JPAQueryFactory 필요하다.
    private final JPAQueryFactory queryFactory;

    // TODO QueryDSL과 EntityManager Injection
    public MemberSpecialQuerydslRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

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

    // TODO 팀이름 동적 쿼리
    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    // TODO 나이 동적 쿼리 크거나 같거나
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe!= null ? member.age.goe(ageGoe) : null;
    }

    // TODO 나이 동적 쿼리 작거나 작거나
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
