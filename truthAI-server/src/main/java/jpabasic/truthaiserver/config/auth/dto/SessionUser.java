package jpabasic.truthaiserver.config.auth.dto;

import jpabasic.truthaiserver.domain.User;
import lombok.Getter;

import java.io.Serializable;

/**
 * 세션에 저장되는 인증 사용자 정보 DTO
 * - 인증된 사용자 정보만 담음 (직렬화 지원)
 */
@Getter
public class SessionUser implements Serializable {

    private String name;
    private String email;
    private String picture;

    /**
     * User 엔티티로부터 필요한 정보만 추출
     */
    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}