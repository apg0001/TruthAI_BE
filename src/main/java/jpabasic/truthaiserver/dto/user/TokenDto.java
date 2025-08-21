package jpabasic.truthaiserver.dto.user;

import lombok.Getter;
import lombok.Setter;

/**
 * 프론트에서 전달하는 구글 OAuth2 로그인 요청 바디
 * - token: authorization code (구글 인가 코드)
 * - redirectUri: 구글 콘솔에 등록된 리다이렉트 URI (code 교환 시 필요)
 */
@Getter
@Setter
public class TokenDto {
    private String token;       // authorization code
    private String redirectUri; // 구글 리다이렉트 URI
}