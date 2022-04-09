package me.ssu.springjpaquerydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.entity.Member;
import me.ssu.springjpaquerydsl.entity.QMember;
import me.ssu.springjpaquerydsl.entity.QTeam;
import me.ssu.springjpaquerydsl.repository.support.Querydsl4RepositorySupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static me.ssu.springjpaquerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberQuerydslRepositorySupport extends Querydsl4RepositorySupport {

    public MemberQuerydslRepositorySupport() {
        //super(domainClass);
        super(Member.class);
    }
    /**
        protected <T> JPAQuery<T> select(Expression<T> expr) {
            return getQueryFactory().select(expr);
        }
    */
    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    /**
         protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
            return getQueryFactory().selectFrom(from);
         }
     */
    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    // TODO 기존 Querydsl4RepositorySupport 페이징 처리할 때
    //  전체 카운트를 한번에 조회하는 단순한 방법
    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition,
                                              Pageable pageable) {

        // TODO limit와 offset을 넣으면 동적으로 sort처리가 안됨.
        JPAQuery<Member> query = selectFrom(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        // TODO sort까지 동적으로 하려면 짜야 하는 코드
        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    /**
        protected <T> Page<T> applyPagination(Pageable pageable,
										  Function<JPAQueryFactory, JPAQuery> contentQuery) {

        // .apply() : 파라미터로 넘어온 게 실행 됨.
		JPAQuery jpaQuery = contentQuery.apply(getQueryFactory());

		List<T> content = getQuerydsl().applyPagination(pageable,
				jpaQuery).fetch();

		return PageableExecutionUtils.getPage(content, pageable,
				jpaQuery::fetchCount);
	    }
    */
    // TODO 내가 작성한 Querydsl4RepositorySupport
    //  전체 카운트를 한번에 조회하는 단순한 방법
    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
        // TODO 커멘트 + OPTION + N == 인라인 합쳐짐.
        Page<Member> result = applyPagination(pageable, query -> query
                .selectFrom(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
        );
        return result;
    }

    // TODO 유저네임 동적 쿼리 활용
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
