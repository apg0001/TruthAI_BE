//package jpabasic.truthaiserver.jwt;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jpabasic.truthaiserver.domain.User;
//import jpabasic.truthaiserver.repository.UserRepository;
//import jpabasic.truthaiserver.security.CustomUserDetails;
//import jpabasic.truthaiserver.service.JwtService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Optional;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository;
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        // 이미 인증되어 있으면 패스
//        if (SecurityContextHolder.getContext().getAuthentication() == null) {
//            String token = jwtService.extractToken(request);
//
//            if (token != null) {
//                try {
//                    if (jwtService.validateAccessToken(token)) {
//                        // 토큰이 유효한 경우
//                        authenticateUser(token, request);
//                    } else {
//                        // 토큰이 만료되었거나 유효하지 않은 경우
//                        log.debug("JWT 토큰이 유효하지 않음");
//                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                        response.getWriter().write("{\"error\":\"토큰이 만료되었거나 유효하지 않습니다.\",\"code\":\"TOKEN_EXPIRED\"}");
//                        return;
//                    }
//                } catch (Exception e) {
//                    // 토큰 파싱 실패 등 예외 발생 시
//                    log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.getWriter().write("{\"error\":\"토큰 형식이 올바르지 않습니다.\",\"code\":\"INVALID_TOKEN\"}");
//                    return;
//                }
//            } else {
//                // 토큰이 없는 경우
//                log.debug("JWT 토큰이 요청에 포함되지 않음");
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("{\"error\":\"인증 토큰이 필요합니다.\",\"code\":\"TOKEN_REQUIRED\"}");
//                return;
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private void authenticateUser(String token, HttpServletRequest request) {
//        try {
//            Long userId = jwtService.getUserIdByParseToken(token);
//            Optional<User> userOpt = userRepository.findById(userId);
//
//            if (userOpt.isPresent()) {
//                User user = userOpt.get();
//                CustomUserDetails principal = new CustomUserDetails(user);
//
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(
//                                principal,
//                                null,
//                                principal.getAuthorities()
//                        );
//                authentication.setDetails(
//                        new WebAuthenticationDetailsSource().buildDetails(request)
//                );
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                log.debug("사용자 인증 성공 - userId: {}", userId);
//            }
//        } catch (Exception e) {
//            log.error("사용자 인증 처리 중 오류 발생: {}", e.getMessage());
//        }
//    }
//}


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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

//@RequiredArgsConstructor
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        String token = jwtService.extractToken(request);
//        if (token != null && jwtService.validateAccessToken(token)) {
//            var userDetails = jwtService.getUserDetailsFromToken(token);
//
//            var authentication = new UsernamePasswordAuthenticationToken(
//                    userDetails, null, userDetails.getAuthorities()
//            );
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository; // ✅ User 조회 위해 주입

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증되어 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = jwtService.extractToken(request);

            if (token != null && jwtService.validateAccessToken(token)) {
                Long userId = jwtService.getUserIdByParseToken(token);

                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // ✅ 엔티티 포함한 UserDetails
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
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}