package jpabasic.truthaiserver.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.repository.UserRepository;
import jpabasic.truthaiserver.security.CustomUserDetails;
import jpabasic.truthaiserver.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증되어 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = jwtService.extractToken(request);

            if (token != null) {
                try {
                    if (jwtService.validateAccessToken(token)) {
                        // 토큰이 유효한 경우
                        authenticateUser(token, request);
                    }
                } catch (Exception e) {
                    // 토큰 검증 실패 시 로그만 남기고 계속 진행
                    log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            Long userId = jwtService.getUserIdByParseToken(token);
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                CustomUserDetails principal = new CustomUserDetails(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("사용자 인증 성공 - userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("사용자 인증 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}