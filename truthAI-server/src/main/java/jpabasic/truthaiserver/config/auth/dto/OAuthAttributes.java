package jpabasic.truthaiserver.config.auth.dto;

import jpabasic.truthaiserver.domain.Role;
import jpabasic.truthaiserver.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * OAuth2 인증 정보 표준화 DTO
 * - 소셜 서비스별 사용자 정보 변환
 */
@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes; // OAuth2 제공 정보
    private String nameAttributeKey;        // PK 속성명
    private String name;
    private String email;
    private String picture;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    /**
     * 서비스별로 사용자 정보 변환 (여기선 구글만 예시)
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        // 서비스 구분에 따라 분기 가능 (ex. 네이버, 카카오 등)
        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * 구글 사용자 정보 변환
     */
    private static OAuthAttributes ofGoogle(String usernameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(usernameAttributeName)
                .build();
    }

    /**
     * User 엔티티로 변환 (회원가입/업데이트용)
     */
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.USER)
                .build();
    }
}