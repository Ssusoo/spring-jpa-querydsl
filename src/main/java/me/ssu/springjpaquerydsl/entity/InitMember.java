package me.ssu.springjpaquerydsl.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    // TODO initMember 실행하기
    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    // TODO 데이터
    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager entityManager;

        // TODO 데이터 초기화
        //  @PostContruct와 @Transactional과 같이 처리할 수가 없음
        //  따로 분리해서 처리해야 함.
        @Transactional
        public void init() {

            // TODO Team 객체 생성
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            // TODO DB에 저장
            entityManager.persist(teamA);
            entityManager.persist(teamB);

            // TODO teamA와 teamB 분리해서 데이터 넣기
            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                entityManager.persist((new Member("member" + i, i, selectedTeam)));
            }
        }
    }
}
