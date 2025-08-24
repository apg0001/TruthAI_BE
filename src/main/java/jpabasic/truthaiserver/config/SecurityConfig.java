//package jpabasic.truthaiserver.config;
//
//import jpabasic.truthaiserver.jwt.JwtFilter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Configurable;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@EnableWebSecurity
//@Configuration
//@Slf4j
//public class SecurityConfig {
//    @Autowired
//    private JwtFilter jwtFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                .authorizeHttpRequests(auth -> auth
//                        // 공개 엔드포인트 (인증 불필요)
//                        .requestMatchers(
//                                "/auth/login",
//                                "/auth/token/refresh",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/error"
//                        ).permitAll()
//                        // 나머지 모든 요청은 인증 필요
//                        .anyRequest().authenticated()
//                )
//                .csrf(AbstractHttpConfigurer::disable)
//                .headers((headers -> headers
//                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
//                )
//                .build();
//    }
//}


package jpabasic.truthaiserver.config;

import jpabasic.truthaiserver.jwt.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                .authorizeHttpRequests(auth -> auth
//                                // api 테스트 위해서 모든 권한 열어둠
////                                .anyRequest().permitAll()
//                        // 실제 배포 시 swagger랑 로그인만 열어둠
//                        .requestMatchers(
//                                "/auth/**",
//                                "/google-test.html",
//                                "/swagger-ui/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//
//                .csrf(AbstractHttpConfigurer::disable)
//                .headers((headers -> headers
//                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
//                )
//                .build();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/google-test.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // 🔥 이 부분 추가
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"TOKEN_EXPIRED\",\"message\":\"토큰이 만료되었습니다.\",\"code\":\"TOKEN_EXPIRED\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"ACCESS_DENIED\",\"message\":\"접근 권한이 없습니다.\",\"code\":\"ACCESS_DENIED\"}");
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                )
                .build();
    }
}