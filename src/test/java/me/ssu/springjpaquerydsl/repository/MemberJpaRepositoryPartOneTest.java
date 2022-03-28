package me.ssu.springjpaquerydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.JpaBaseTest;
import me.ssu.springjpaquerydsl.dto.MemberSearchCondition;
import me.ssu.springjpaquerydsl.dto.MemberTeamDto;
import me.ssu.springjpaquerydsl.entity.Member;
import me.ssu.springjpaquerydsl.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemberJpaRepositoryPartOneTest extends JpaBaseTest {

    /**
     * 순수 JPA Test
     *  Repository에서 Query문이 오타가 나도
     *  1) 고객이 실수하는 Runtime 시점이 되서야 알 수 있다는 단점이 있음.
     *  2) .setParameter("username", username)의 단점이 있음.
     */
    // TODO 여기를 빠져나가면 트랜잭션에서 세션 밖으로 나왔을 때
    //  Detached(JPA가 더이상 관리하지 않음) 상태가 되는 거임.
    @Test
    @DisplayName("순수 JPA - Test")
    void basicTest() {
        // TODO Member 객체 생성(Transient)
        //  new해서 객체를 만든 경우가 Transient 상태이며,
        //  Hibername와 JPA가 전혀 모르는 상태 DB에 맵핑되어 있는 레코드가 없음.
        Member member = new Member("member1", 10);

        // TODO Insert Query(Persistent)
        //  이렇게 save() 저장을 하면 JPA가 관리중인 상태임.
        //  Persistent 상태일 때 JPA(Hibernate)는 1차 캐시
        //  1차 캐시는 트랜잭션을 시작하고 종료할 때까지만 적용된다.
        //  영속성 컨텍스트(변경사항과 DB 상태를 맞추는 거임) 내부에 엔티티를 저장소가 있는데 이를 1차 캐시라고 함.
        memberJpaRepository.save(member);

        // TODO 개별조회(Select Query)
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        // TODO 전체 조회(Select Query)
        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);    // member를 가지고 있냐

        // TODO 개별조회(Select Query)
        //  findByUsername(String username) == member1
        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    /**
     * QueryDSL Test
     *  1) Compile 시점에서 오타를 발견할 수 있기 때문에 장점임.
     *  2) .where(member.username.eq(username)) 기본적으로 파라미터 바인딩이라는 장점.
     */
    @Test
    @DisplayName("Querydsl - Test")
    void basicQuerydslTest() {
        // TODO Member 객체 생성(Transient)
        //  new해서 객체를 만든 경우가 Transient 상태이며,
        //  Hibername와 JPA가 전혀 모르는 상태 DB에 맵핑되어 있는 레코드가 없음.
        Member member = new Member("member1", 10);

        // TODO Insert Query(Persistent)
        //  이렇게 save() 저장을 하면 JPA가 관리중인 상태임.
        //  Persistent 상태일 때 JPA(Hibernate)는 1차 캐시
        //  1차 캐시는 트랜잭션을 시작하고 종료할 때까지만 적용된다.
        //  영속성 컨텍스트(변경사항과 DB 상태를 맞추는 거임) 내부에 엔티티를 저장소가 있는데 이를 1차 캐시라고 함.
        memberJpaRepository.save(member);

        // TODO 전체 조회(Select Query)
        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        // TODO 개별조회(Select Query)
        //  findByUsername_Querydsl(String username) == member1
        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);
    }
    /**
     *  동적 쿼리와 성능 테스트 Test
     */
    @Test
    void searchTest() {
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

        // TODO 검색 조건
        MemberSearchCondition condition = new MemberSearchCondition();

        // TODO 조건 전부(동적쿼리)
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        // TODO 전체 조건에서 teamB만 가져올 때(동적쿼리)
//        condition.setTeamName("teamB");

        // TODO 동적쿼리와 성능 최적화
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        // TODO 조건 전부
        assertThat(result).extracting("username").containsExactly("member4");

        // TODO 전체 조건에서 teamB만 가져올 때
//        assertThat(result).extracting("username").containsExactly("member3", "member4");
    }
}
