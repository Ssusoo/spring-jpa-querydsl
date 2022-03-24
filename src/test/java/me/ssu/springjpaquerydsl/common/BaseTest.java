package me.ssu.springjpaquerydsl.common;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@SpringBootTest
@Transactional
@Disabled
public class BaseTest {

    @Autowired
    protected EntityManager entityManager;

    // TODO field level로 빼기
    protected JPAQueryFactory queryFactory;
}
