package me.ssu.springjpaquerydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.dto.QMemberTeamDto;
import me.ssu.springjpaquerydsl.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static me.ssu.springjpaquerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

/**
 * 사용자 정의 인터페이스 구현(커스텀한 QueryDSL)-2.
 */
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
        //  PageImpl이 스프링 데이터 JPA Page의 구현체임.
        return new PageImpl<>(content, pageable, total);
    }

    // TODO 스프링 데이터 JPA에서 제공하는 페이징 처리(Page, Pageable)-2-2
    //  데이터 내용과 전체 카운트를 별도로 조회하는 방법
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        // TODO 전체 카운터를 조회하는 방법과 달리
        //  QueryResults<MemberTeamDto> -> List<MemberTeamDto>로 변경-1
        //  전체 카운터를 조회하는 방법에서는 fetchResults를 이용해 QueryDSL이 Total Count를
        //  알아서 날리게 했지만 이 방법은 내가 직접 totalCount를 날리는 거임.
        List<MemberTeamDto> content = queryFactory
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
                // TODO Where절에 파라미터(동적쿼리)
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
                // TODO fetchResults -> fetch로 변경하기-2
                .fetch();
        // TODO totalCount Query를 추가로 만들기-3
        //  내가 직접 날리는 totalCount의 장점은
        //  가끔 Join이 필요 없을 때가 있음.
        //  content는 어렵지만 totalCount는 쉽게 처리하는 경우가 생긴다.
        //  이번 방법에서 성능 최적화가 가능함. 위의 방법에서는 성능 최적화를 하지 못한다.
        //  이유는 QueryDsl이 totalCount까지 같이 날리고 Join까지 다 되기 때문에
        long total = queryFactory.select(member)
                .from(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetchCount();
        // TODO 데이터 반환하기
        //  PageImpl이 스프링 데이터 JPA Page의 구현체임.
        return new PageImpl<>(content, pageable, total);
    }


    // TODO Count 쿼리 최적화
    @Override
    public Page<MemberTeamDto> searchPagePerformanceOptimization(MemberSearchCondition condition,
                                                                 Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(
                        new QMemberTeamDto (
                        // TODO 멤버는 필드명의 아이디이기 때문에 as()
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                        )
                )
                .from(member)
                // TODO Member와 Team Join하기(Team의 데이터를 다 가져오기 때문에
                .leftJoin(member.team, team)
                // TODO Where절에 파라미터(동적쿼리)
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
                // TODO 리스트 조회, 데이터 없으면 빈 리스트 반환
                .fetch();

        // TODO totalCount
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        // TODO 데이터 반환하기
        //  PageImpl이 스프링 데이터 JPA Page의 구현체임.
//        return new PageImpl<>(content, pageable, total);


        // TODO 자바 8에서 fetchCount()를 해야 실제 countQuery가 날라감.
//        countQuery.fetchCount();

        // TODO PageableExcutionUtils를 통해 countent와 pageable과 함수(countQuery.fetchCount())를 넘기면 됨.
        //  함수이기 때문에 구분이 실행이 안된다. content와 pageable의 totalcount를 보고 난 후
        //  페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작으면 함수(countQuery.fetchCount()) 자체를 실행하지 않는다.
        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
//        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }
}
