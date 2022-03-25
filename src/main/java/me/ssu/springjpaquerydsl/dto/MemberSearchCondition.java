package me.ssu.springjpaquerydsl.dto;

import lombok.Data;

/* TODO @Data
    Getter, Setter, RequiredArgsConstructor,
    ToString, EqualsAndHashCode
*/
@Data
public class MemberSearchCondition {
    // TODO 회원명, 팀명, 나이(ageGoe >= , ageLoe <=)

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;

}
