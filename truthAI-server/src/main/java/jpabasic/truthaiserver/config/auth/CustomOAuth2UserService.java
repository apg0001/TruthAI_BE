package jpabasic.truthaiserver.config.auth;

import jpabasic.truthaiserver.config.auth.dto.OAuthAttributes;
import jpabasic.truthaiserver.config.auth.dto.SessionUser;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * OAuth2 로그인 사용자 정보 처리 서비스
 * - 소셜 로그인 사용자 정보 DB 저장/업데이트
 * - 세션에 인증 정보 저장
 */
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    /**
     * OAuth2 로그인 사용자 정보 로드
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2UserService로 사용자 정보 조회
        OAuth2User delegate = new DefaultOAuth2UserService().loadUser(userRequest);

        // 2. 서비스 구분(구글, 네이버 등) 및 PK 속성명 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 3. OAuthAttributes로 표준화
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, delegate.getAttributes());

        // 4. 사용자 정보 저장/업데이트 및 세션 저장
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        // 5. Spring Security 인증 객체 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    /**
     * 사용자 정보 DB 저장/업데이트
     */
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());
        return userRepository.save(user);
    }
}