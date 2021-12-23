package me.ssu.springjpaquerydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.entity.Hello;
import me.ssu.springjpaquerydsl.entity.QHello;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SpringJpaQuerydslApplicationTests {

    @Autowired
    EntityManager entityManager;

    @Test
    void contextLoads() {
        Hello hello = new Hello();
        // Entity 저장
        entityManager.persist(hello);

        // TODO Querydsl(JPAQueryFactory)-1
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // TODO Querydsl(QHello)-2
        QHello qHello = QHello.hello;
//        QHello qHello = new QHello("h");

        // TODO Querydsl(JPAQueryFactory + featchOne())-3
        Hello result = queryFactory.selectFrom(qHello)
                .fetchOne();

        // TODO Querydsl 검증
        assertThat(result).isEqualTo(hello);

        // TODO Lombok 검증
        assertThat(result.getId()).isEqualTo(hello.getId());
    }
}
