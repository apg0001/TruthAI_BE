package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.dto.user.GoogleInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final WebClient.Builder webClientBuilder;

    /**
     * authorization code를 받아 access_token → userinfo 순으로 조회하여 사용자 정보를 반환
     */
    public GoogleInfoDto authenticate(String authorizationCode, String redirectUri) {
        WebClient webClient = webClientBuilder.build();

        // 1) authorization code로 access_token 교환
        TokenResponse token = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", authorizationCode)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        if (token == null || token.access_token == null) {
            throw new RuntimeException("구글 access_token 발급 실패");
        }

        // 2) access_token으로 userinfo 조회
        GoogleUserInfoResponse user = webClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .headers(h -> h.setBearerAuth(token.access_token))
                .retrieve()
                .bodyToMono(GoogleUserInfoResponse.class)
                .block();

        if (user == null || user.email == null) {
            throw new RuntimeException("구글 userinfo 조회 실패");
        }

        return new GoogleInfoDto(user.email, user.name != null ? user.name : user.sub, user.picture);
    }

    // token 응답 DTO (내부 전용)
    static class TokenResponse {
        public String access_token;
        public String id_token;
        public String refresh_token;
        public String token_type;
        public Integer expires_in;
    }

    // userinfo 응답 DTO (내부 전용)
    static class GoogleUserInfoResponse {
        public String sub;      // 구글 고유 ID
        public String email;
        public String name;
        public String picture;
    }
}