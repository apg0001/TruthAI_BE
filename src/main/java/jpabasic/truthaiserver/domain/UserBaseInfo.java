package jpabasic.truthaiserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Embeddable
@NoArgsConstructor  // 기본 생성자
@Slf4j
@Getter
public class UserBaseInfo {
    @Column(nullable = false, unique = true, length=50)
    private String email;

    @Column(length = 30)
    private String nickname;

    private String profilePictureUrl;

    @Column
    private String persona;

    public UserBaseInfo(String email, String nickname, String profilePictureUrl) {
        this.email = email;
        this.nickname = nickname;
        this.profilePictureUrl = profilePictureUrl;
    }

    public void updatePersona(String persona) {
        this.persona = persona;
    }
}
