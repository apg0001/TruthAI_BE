package jpabasic.truthaiserver.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한(역할) Enum
 */
@Getter
@RequiredArgsConstructor
public enum Role {
    /**
     * 관리자 권한
     */
    ADMIN("ROLE_ADMIN", "관리자"),
    /**
     * 일반 사용자 권한
     */
    USER("ROLE_USER", "사용자");

    private final String key;   // Spring Security 권한명
    private final String title; // 한글 설명
}
