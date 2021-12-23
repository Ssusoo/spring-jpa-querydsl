package me.ssu.springjpaquerydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import me.ssu.springjpaquerydsl.common.BaseTest;
import me.ssu.springjpaquerydsl.entity.Hello;
import me.ssu.springjpaquerydsl.entity.QHello;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest extends BaseTest {

    @Test
    void contextLoads() {
        // TODO Given, Entity 저장
        Hello hello = new Hello();
        entityManager.persist(hello);

        // TODO When, Querydsl(JPAQueryFactory)-1
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        // TODO When, Querydsl(QHello)-2
        QHello qHello = QHello.hello;
//        QHello qHello = new QHello("h");

        // TODO When, Querydsl(JPAQueryFactory + featchOne())-3
        Hello result = queryFactory.selectFrom(qHello)
                .fetchOne();

        // TODO Then, Querydsl 검증
        assertThat(result).isEqualTo(hello);

        // TODO Then, Lombok 검증
        assertThat(result.getId()).isEqualTo(hello.getId());
    }
}
