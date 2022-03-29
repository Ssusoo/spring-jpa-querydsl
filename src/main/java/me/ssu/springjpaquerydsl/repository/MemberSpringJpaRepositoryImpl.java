package me.ssu.springjpaquerydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.dto.QMemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    // TODO 스프링 데이터 JPA에서 제공하는 페이징 처리(Page, Pageable)-2-1
    //  전체 카운트를 한번에 조회하는 단순한 방법
    //  파라미터로 Pageable이 넘어오는데 기본적으로 of이나 전체 페이지 수를 알 수 있다.
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
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
                // TODO 몇 번을 스킵하고 몇 번부터 시작할 거야.
                .offset(pageable.getOffset())
                // TODO 한 번 조회할 때 몇 개까지 조회할 거야.
                .limit(pageable.getPageSize())
                /* TODO 결과조회 fetch -> fetchResult
                    - fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
                    - fetchOne() : 단 건 조회
                    - 결과가 없으면 : null
                    - 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
                    - fetchFirst() : limit(1).fetchOne()
                    - fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
                    - fetchCount() : count 쿼리로 변경해서 count 수 조회
                */
                .fetchResults();
        // TODO 데이터 꺼내기(실제 데이터가 되는 거임) 쿼리 한 방
        List<MemberTeamDto> content = results.getResults();

        // TODO total count 쿼리 두
        long total = results.getTotal();

        // TODO 데이터 반환하기
        return new PageImpl<>(condition, pageable, total)

    }

    // TODO 스프링 데이터 JPA에서 제공하는 페이징 처리(Page, Pageable)-2-2
    //  데이터 내용과 전체 카운트를 별도로 조회하는 방법
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        return null;
    }
}
