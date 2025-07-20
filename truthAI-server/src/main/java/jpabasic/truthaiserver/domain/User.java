package jpabasic.truthaiserver.domain;

import jakarta.persistence.*; // JPA 엔티티 매핑
import jakarta.validation.constraints.NotNull; // 필드 검증
import lombok.*; // 롬복

/**
 * 사용자 엔티티 (회원 정보)
 * JPA, Validation, Lombok을 활용한 최신 스타일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    /**
     * PK (자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * 사용자 이름 (필수)
     */
    @NotNull
    private String name;

    /**
     * 이메일 (필수, 중복 불가)
     */
    @NotNull
    private String email;

    /**
     * 프로필 사진 URL (선택)
     */
    @Column
    private String picture;

    /**
     * 권한(역할) - Enum은 문자열로 저장
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    /**
     * 빌더 패턴 생성자
     */
    @Builder
    public User(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }

    /**
     * 사용자 정보(이름, 사진) 업데이트
     */
    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    /**
     * 권한 키 반환 (Spring Security 권한 체크용)
     */
    public String getRoleKey() {
        return this.role.getKey();
    }
}