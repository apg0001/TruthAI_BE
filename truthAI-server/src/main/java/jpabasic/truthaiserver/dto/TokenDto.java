package jpabasic.truthaiserver.dto;

import lombok.Getter;
import lombok.Setter;

// 내부 클래스든 별도 클래스든 상관 없음
@Getter
@Setter
public class TokenDto {
    private String token;
}