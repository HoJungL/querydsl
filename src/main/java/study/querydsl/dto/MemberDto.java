package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 이거 있으면 기본 생성자 필요 없음.
public class MemberDto {

    private String username;
    private int age;


    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
