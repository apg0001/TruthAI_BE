package jpabasic.truthaiserver.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 보안 설정
 * - OAuth2, CSRF, 권한, 로그아웃 등
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    /**
     * HTTP 보안 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (API/테스트용)
            .csrf(csrf -> csrf.disable())
            // H2 콘솔 등 프레임 허용
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            // URL별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/login/**").permitAll() // 전체 허용
                .anyRequest().authenticated() // 나머지는 인증 필요
            )
            // 로그아웃 성공 시 메인으로 이동
            .logout(logout -> logout.logoutSuccessUrl("/"))
            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/") // 로그인 성공 시 메인으로 이동
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
            );
        return http.build();
    }
}