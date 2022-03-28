package me.ssu.springjpaquerydsl.common;


import me.ssu.springjpaquerydsl.repository.MemberRealJpaRepository;
import me.ssu.springjpaquerydsl.repository.MemberSpringJpaRepository;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
@Disabled
public class JpaBaseTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected MemberRealJpaRepository memberJpaRepository;

    @Autowired
    protected MemberSpringJpaRepository memberSpringJpaRepository;
}
