package me.ssu.springjpaquerydsl.repository;


import me.ssu.springjpaquerydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// TODO 스프링 데이터 JPA(Interface끼리 상속받음)
// TODO 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
//  스프링 데이터 JPA + QueryDSL 사용하기
//  인터페이스는 여러 개를 상속받을 수 있음
public interface MemberSpringJpaRepository extends JpaRepository<Member, Long>, MemberSpringJpaRepositoryCustom {
    // TODO findByUsername(select m from Member m where m.username=?)
    //  기존 순수 JPA(MemberJpaRepository)에서 만든 대부분 코드가 사라짐(findByUsername은 만들어주어야 함)
    List<Member> findByUsername(String username);
}
