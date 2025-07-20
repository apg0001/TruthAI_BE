package jpabasic.truthaiserver.repository;

import jpabasic.truthaiserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 JPA 레포지토리
 * 기본 CRUD + 이메일로 사용자 조회
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이메일로 사용자 조회 (중복 가입 방지)
     */
    Optional<User> findByEmail(String email);
}
