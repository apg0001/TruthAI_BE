package jpabasic.truthaiserver.service;

import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.PersonaRequest;
import jpabasic.truthaiserver.dto.PersonaResponse;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static jpabasic.truthaiserver.exception.ErrorMessages.USER_NULL_ERROR;

@Service
@RequiredArgsConstructor
public class UserFindService {
    private final UserRepository userRepository;

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByUserBaseInfo_Email(email);
    }

    @Transactional
    public PersonaResponse setPersona(PersonaRequest personaRequest,String email) {
        User user=findUserByEmail(email)
                .orElseThrow(()->new BusinessException(USER_NULL_ERROR));

        user.getUserBaseInfo().updatePersona(personaRequest.persona());
        return new PersonaResponse(user.getUserBaseInfo().getPersona());
    }


}