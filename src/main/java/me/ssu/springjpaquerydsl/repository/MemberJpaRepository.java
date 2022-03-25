package me.ssu.springjpaquerydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

// TODO DAO와 같은 개념(Entity 조회하기 위한 어떤 계층)
@Repository
public class MemberJpaRepository {

    // TODO 순수 JPA이기 때문에 EntityManager가 필요함
    private final EntityManager entityManager;
    // TODO QueryDSL을 사용하려면 JpaQueryFactory가 필요함.
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

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

    // TODO 전체 조회
    public List<Member> findAll() {
        return entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // TODO username으로 조회
    public List<Member> findByUsername(String username) {
        return entityManager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
}
