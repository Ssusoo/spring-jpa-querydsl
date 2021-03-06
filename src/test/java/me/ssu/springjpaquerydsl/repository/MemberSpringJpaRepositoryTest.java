package me.ssu.springjpaquerydsl.repository;

import me.ssu.springjpaquerydsl.common.JpaBaseTest;
import me.ssu.springjpaquerydsl.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemberSpringJpaRepositoryTest extends JpaBaseTest {

    @Test
    @DisplayName("순수 JPA에서 스프링 데이터 JPA")
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
        memberSpringJpaRepository.save(member);

        // TODO 개별조회(Select Query)
        Member findMember = memberSpringJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        // TODO 전체 조회(Select Query)
        List<Member> result1 = memberSpringJpaRepository.findAll();
        assertThat(result1).containsExactly(member);    // member를 가지고 있냐

        // TODO 개별조회(Select Query)
        //  findByUsername(String username) == member1
        List<Member> result2 = memberSpringJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }
}
