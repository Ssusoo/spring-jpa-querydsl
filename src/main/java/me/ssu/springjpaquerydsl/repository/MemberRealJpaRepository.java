package me.ssu.springjpaquerydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.dto.QMemberTeamDto;
import me.ssu.springjpaquerydsl.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static me.ssu.springjpaquerydsl.entity.QMember.member;
import static me.ssu.springjpaquerydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

/**
 * 순수 JPA(!= 스프링 데이터 JPA와 다르다).
 */
// TODO DAO와 같은 개념(Entity 조회하기 위한 어떤 계층)
@Repository
// TODO 생성자 생성없이 QueryDSL 활용하기!
//@RequiredArgsConstructor
public class MemberRealJpaRepository {

    // TODO 순수 JPA이기 때문에 EntityManager가 필요함
    private final EntityManager entityManager;
    // TODO QueryDSL을 사용하려면 JpaQueryFactory가 필요함.
    private final JPAQueryFactory queryFactory;

    // TODO 생성자 사용하는 방법
    public MemberRealJpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    // TODO 생성자에서 주입하는 방법
//    public MemberJpaRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
//        this.entityManager = entityManager;
//        this.queryFactory = queryFactory;
//    }

    // TODO JPA Save
    public void save(Member member) {
        entityManager.persist(member);
    }

    // TODO 개별 조회
    public Optional<Member> findById(Long id) {
        Member findMember = entityManager.find(Member.class, id);
        // TODO Null일 수도 있기 때문에
        return Optional.ofNullable(findMember);
    }

    // TODO 전체 조회(순수 JPA)
    public List<Member> findAll() {
        return entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // TODO 전체 조회(QueryDSL)
    public List<Member> findAll_Querydsl() {
        return queryFactory
                // TODO QMember.member하고 static import 해서 member로 바꾸기
                .selectFrom(member)
                .fetch();
    }

    // TODO username으로 조회(순수 JPA)
    public List<Member> findByUsername(String username) {
        return entityManager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
    // TODO username으로 조회(QueryDSL)
    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    // TODO MemberSearchCondition 검색조건(동적 쿼리와 성능 최적화)
    //  DTO 조회시 Entity로 조회하는 게 아니며,
    //  지연로딩을 사용할 수 없기 때문에 N+1 문제가 발생하지 않음.
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        // TODO 아래 조건들이 돌아갈 수 있게 Builder 만들기-2
        BooleanBuilder builder = new BooleanBuilder();

        // TODO import org.springframework.util.StringUtils; static import로 줄이기
        //  회원명 조건
        if (hasText(condition.getUsername())) {     // null, ""일 수도 있음.
            builder.and(member.username.eq(condition.getUsername()));
        }

        // TODO 팀명 조건
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        // TODO 특정 나이 이상일 때 조건
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeLoe()));
        }

        // TODO 특정 나이 이하일 때 조건
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        // TODO 빌더를 통해 동적쿼리 처리하기-1
        return queryFactory
                .select(new QMemberTeamDto(
                        // TODO 멤버는 필드명의 아이디이기 때문에 as()
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                // TODO Member와 Team Join하기(Team의 데이터를 다 가져오기 때문에
                .leftJoin(member.team, team)
                // TODO builder 빼먹지 말기-3
                .where(builder)
                .fetch();
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

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }


    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe!= null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    // TODO Where절의 장점(Entity로 조회할 때 조립도 가능함)
    public List<Member> searchMember(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                // TODO 위에서 썼던 메소드를 재사용할 수 있는 장점-1
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        // TODO 조립이 가능하다-3(Null 체크만 조심하면 됨)
                        ageBetween(condition.getAgeLoe(), condition.getAgeGoe())
                )
                .fetch();
    }

    // TODO Where절의 장점(조립이 가능하다)-2
    private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
        return ageGoe(ageLoe).and(ageGoe(ageGoe));
    }
}
