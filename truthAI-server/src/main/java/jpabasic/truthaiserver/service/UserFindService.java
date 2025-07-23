package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFindService {
    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByUserBaseInfo_Email(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByUserBaseInfo_Email(email).isPresent();
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByUserBaseInfo_Email(email);
    }
}