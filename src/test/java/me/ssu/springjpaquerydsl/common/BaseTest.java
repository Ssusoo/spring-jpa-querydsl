package me.ssu.springjpaquerydsl.common;

import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
@Disabled
public class BaseTest {

    @Autowired
    protected EntityManager entityManager;
}
