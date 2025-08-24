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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // permitAll 경로 및 프리플라이트는 필터 자체를 건너뜀
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        return uri.equals("/auth")
                || uri.startsWith("/auth/")
                || uri.equals("/google-test.html")
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증되어 있으면 통과
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출
        String token = null;
        try {
            token = jwtService.extractToken(request);
        } catch (Exception e) {
            log.debug("Authorization 헤더 파싱 실패: {}", e.getMessage());
        }

        // 토큰이 있고 유효한 경우에만 인증 설정
        if (token != null) {
            try {
                if (jwtService.validateAccessToken(token)) {
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
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("JWT 인증 성공 - userId: {}", userId);
                    } else {
                        log.debug("JWT 사용자 조회 실패 - userId: {}", userId);
                    }
                } else {
                    log.debug("JWT 유효성 실패(만료/서명오류 등)");
                }
            } catch (Exception e) {
                log.debug("JWT 검증 예외: {}", e.getMessage());
            }
        } else {
            log.debug("JWT 토큰 없음");
        }

        // 🔥 토큰이 없거나 유효하지 않을 때는 익명 인증 설정
        // 이렇게 하면 Spring Security가 요청을 계속 처리할 수 있음
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // 익명 인증 설정 (Spring Security 기본값)
            SecurityContextHolder.getContext().setAuthentication(null);
        }

        // 항상 다음 필터로 진행 (응답 종료하지 않음)
        filterChain.doFilter(request, response);
    }
}