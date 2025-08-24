////package jpabasic.truthaiserver.jwt;
////
////import jakarta.servlet.FilterChain;
////import jakarta.servlet.ServletException;
////import jakarta.servlet.http.HttpServletRequest;
////import jakarta.servlet.http.HttpServletResponse;
////import jpabasic.truthaiserver.domain.User;
////import jpabasic.truthaiserver.repository.UserRepository;
////import jpabasic.truthaiserver.security.CustomUserDetails;
////import jpabasic.truthaiserver.service.JwtService;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.core.context.SecurityContextHolder;
////import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
////import org.springframework.stereotype.Component;
////import org.springframework.web.filter.OncePerRequestFilter;
////
////import java.io.IOException;
////import java.util.Optional;
////
////@Slf4j
////@RequiredArgsConstructor
////@Component
////public class JwtFilter extends OncePerRequestFilter {
////
////    private final JwtService jwtService;
////    private final UserRepository userRepository;
////
////    @Override
////    protected void doFilterInternal(
////            HttpServletRequest request,
////            HttpServletResponse response,
////            FilterChain filterChain
////    ) throws ServletException, IOException {
////
////        // 이미 인증되어 있으면 패스
////        if (SecurityContextHolder.getContext().getAuthentication() == null) {
////            String token = jwtService.extractToken(request);
////
////            if (token != null) {
////                try {
////                    if (jwtService.validateAccessToken(token)) {
////                        // 토큰이 유효한 경우
////                        authenticateUser(token, request);
////                    } else {
////                        // 토큰이 만료되었거나 유효하지 않은 경우
////                        log.debug("JWT 토큰이 유효하지 않음");
////                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
////                        response.getWriter().write("{\"error\":\"토큰이 만료되었거나 유효하지 않습니다.\",\"code\":\"TOKEN_EXPIRED\"}");
////                        return;
////                    }
////                } catch (Exception e) {
////                    // 토큰 파싱 실패 등 예외 발생 시
////                    log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
////                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
////                    response.getWriter().write("{\"error\":\"토큰 형식이 올바르지 않습니다.\",\"code\":\"INVALID_TOKEN\"}");
////                    return;
////                }
////            } else {
////                // 토큰이 없는 경우
////                log.debug("JWT 토큰이 요청에 포함되지 않음");
////                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
////                response.getWriter().write("{\"error\":\"인증 토큰이 필요합니다.\",\"code\":\"TOKEN_REQUIRED\"}");
////                return;
////            }
////        }
////
////        filterChain.doFilter(request, response);
////    }
////
////    private void authenticateUser(String token, HttpServletRequest request) {
////        try {
////            Long userId = jwtService.getUserIdByParseToken(token);
////            Optional<User> userOpt = userRepository.findById(userId);
////
////            if (userOpt.isPresent()) {
////                User user = userOpt.get();
////                CustomUserDetails principal = new CustomUserDetails(user);
////
////                UsernamePasswordAuthenticationToken authentication =
////                        new UsernamePasswordAuthenticationToken(
////                                principal,
////                                null,
////                                principal.getAuthorities()
////                        );
////                authentication.setDetails(
////                        new WebAuthenticationDetailsSource().buildDetails(request)
////                );
////
////                SecurityContextHolder.getContext().setAuthentication(authentication);
////                log.debug("사용자 인증 성공 - userId: {}", userId);
////            }
////        } catch (Exception e) {
////            log.error("사용자 인증 처리 중 오류 발생: {}", e.getMessage());
////        }
////    }
////}
//
//
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
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Optional;
//
////@RequiredArgsConstructor
////@Component
////public class JwtFilter extends OncePerRequestFilter {
////
////    private final JwtService jwtService;
////
////    @Override
////    protected void doFilterInternal(HttpServletRequest request,
////                                    HttpServletResponse response,
////                                    FilterChain filterChain) throws ServletException, IOException {
////        String token = jwtService.extractToken(request);
////        if (token != null && jwtService.validateAccessToken(token)) {
////            var userDetails = jwtService.getUserDetailsFromToken(token);
////
////            var authentication = new UsernamePasswordAuthenticationToken(
////                    userDetails, null, userDetails.getAuthorities()
////            );
////            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
////
////            SecurityContextHolder.getContext().setAuthentication(authentication);
////        }
////
////        filterChain.doFilter(request, response);
////    }
////}
//
//@RequiredArgsConstructor
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository; // ✅ User 조회 위해 주입
//
////    @Override
////    protected void doFilterInternal(
////            HttpServletRequest request,
////            HttpServletResponse response,
////            FilterChain filterChain
////    ) throws ServletException, IOException {
////
////        // 이미 인증되어 있으면 패스
////        if (SecurityContextHolder.getContext().getAuthentication() == null) {
////            String token = jwtService.extractToken(request);
////
////            if (token != null && jwtService.validateAccessToken(token)) {
////                Long userId = jwtService.getUserIdByParseToken(token);
////
////                Optional<User> userOpt = userRepository.findById(userId);
////                if (userOpt.isPresent()) {
////                    User user = userOpt.get();
////
////                    // 엔티티 포함한 UserDetails
////                    CustomUserDetails principal = new CustomUserDetails(user);
////
////                    UsernamePasswordAuthenticationToken authentication =
////                            new UsernamePasswordAuthenticationToken(
////                                    principal,
////                                    null,
////                                    principal.getAuthorities()
////                            );
////                    authentication.setDetails(
////                            new WebAuthenticationDetailsSource().buildDetails(request)
////                    );
////
////                    SecurityContextHolder.getContext().setAuthentication(authentication);
////                }
////            }
////        }
////
////        filterChain.doFilter(request, response);
////    }
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
//                        Long userId = jwtService.getUserIdByParseToken(token);
//                        Optional<User> userOpt = userRepository.findById(userId);
//                        if (userOpt.isPresent()) {
//                            User user = userOpt.get();
//                            CustomUserDetails principal = new CustomUserDetails(user);
//                            UsernamePasswordAuthenticationToken authentication =
//                                    new UsernamePasswordAuthenticationToken(
//                                            principal, null, principal.getAuthorities()
//                                    );
//                            authentication.setDetails(
//                                    new WebAuthenticationDetailsSource().buildDetails(request)
//                            );
//                            SecurityContextHolder.getContext().setAuthentication(authentication);
//                        }
//                    } else {
//                        // 토큰이 만료되었거나 유효하지 않은 경우
//                        response.setStatus(401);
//                        response.setContentType("application/json;charset=UTF-8");
//                        response.getWriter().write("{\"error\":\"TOKEN_EXPIRED\",\"message\":\"토큰이 만료되었습니다.\",\"code\":\"TOKEN_EXPIRED\"}");
//                        return;
//                    }
//                } catch (Exception e) {
//                    // 토큰 파싱 실패 등 예외 발생 시
//                    response.setStatus(401);
//                    response.setContentType("application/json;charset=UTF-8");
//                    response.getWriter().write("{\"error\":\"INVALID_TOKEN\",\"message\":\"유효하지 않은 토큰입니다.\",\"code\":\"INVALID_TOKEN\"}");
//                    return;
//                }
//            } else {
//                // 토큰이 없는 경우
//                response.setStatus(401);
//                response.setContentType("application/json;charset=UTF-8");
//                response.getWriter().write("{\"error\":\"TOKEN_REQUIRED\",\"message\":\"인증 토큰이 필요합니다.\",\"code\":\"TOKEN_REQUIRED\"}");
//                return;
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String uri = request.getRequestURI();
//        // 필요 시 /v3/api-docs, /swagger-ui 등도 추가
//        return uri.equals("/auth") ||
//                uri.startsWith("/auth/") ||
//                uri.equals("/google-test.html") ||
//                uri.startsWith("/swagger-ui/") ||
//                uri.startsWith("/v3/api-docs");
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
            token = jwtService.extractToken(request); // ex) "Authorization: Bearer xxx"에서 토큰 반환, 없으면 null
        } catch (Exception e) {
            // 헤더 파싱 오류 등은 로깅만 하고 통과 (에러 응답은 EntryPoint가 담당)
            log.debug("Authorization 헤더 파싱 실패: {}", e.getMessage());
        }

        // 토큰 없으면 인증 시도하지 않고 그대로 통과 (보호 경로에서는 EntryPoint가 401 처리)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰이 있으면 검증
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
                filterChain.doFilter(request, response);
            } else {
                // 유효하지 않은 토큰: 응답을 여기서 만들지 말고 그대로 통과 (보호 경로면 EntryPoint가 401)
                log.debug("JWT 유효성 실패(만료/서명오류 등)");
            }
        } catch (Exception e) {
            // 토큰 파싱/검증 중 예외: 여기서 응답 쓰지 않고 그대로 통과
            log.debug("JWT 검증 예외: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}