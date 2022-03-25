package me.ssu.springjpaquerydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

/* TODO @Data
    Getter, Setter, RequiredArgsConstructor,
    ToString, EqualsAndHashCode
*/
@Data
public class MemberTeamDto {

    // TODO Member
    private Long memberId;
    private String username;
    private int age;

    // TODO Team
    private Long teamId;
    private String teamName;

    // TODO (other compileQuerydsl : DTO도 Q File로 생성 됨
    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
