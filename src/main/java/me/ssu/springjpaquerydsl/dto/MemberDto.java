package me.ssu.springjpaquerydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
// TODO 기본생성자 만들기(getConstructor 오류 해결)
@NoArgsConstructor
public class MemberDto {

    // TODO Entity(Member의 값을 다 불러야 함)를 조회하는 게 아니라
    //  딱 두 개만 프로젝션하고 싶을 때
    private String username;
    private int age;

    // TODO (other compileQuerydsl : DTO도 Q File로 생성 됨
    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
