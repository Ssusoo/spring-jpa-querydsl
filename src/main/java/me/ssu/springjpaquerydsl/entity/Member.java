package me.ssu.springjpaquerydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
// TODO JPA 기본 생성자가 필수(Protected까지 허용)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// TODO 연관관계는 빼주고 toString
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;
    private int age;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this(username, 0, null);
    }

    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    private void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
