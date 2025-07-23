package jpabasic.truthaiserver.service;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.UserRegistrationDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRegisterService {

    private final UserRepository userRepository;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto){
        User newUser = registrationDto.toEntity();
        User saved = userRepository.save(newUser);

        log.info("✅ 신규 사용자 저장됨: id={}, email={}, nickname={}",
                saved.getId(), saved.getUserBaseInfo().getEmail(), saved.getUserBaseInfo().getNickname());
        try {
            return userRepository.save(newUser);
        }catch(RuntimeException e){
    throw new BusinessException(ErrorMessages.REGISTRATION_FAILED);
        }
    }
}
