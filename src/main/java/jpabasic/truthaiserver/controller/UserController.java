package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.domain.UserBaseInfo;
import jpabasic.truthaiserver.dto.GoogleInfoDto;
import jpabasic.truthaiserver.dto.PersonaRequest;
import jpabasic.truthaiserver.dto.PersonaResponse;
import jpabasic.truthaiserver.dto.TokenDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.UserRepository;
import jpabasic.truthaiserver.service.LoginService;
import jpabasic.truthaiserver.service.UserFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jpabasic.truthaiserver.service.AuthService;


import java.util.Map;

import static jpabasic.truthaiserver.exception.ErrorMessages.USER_NULL_ERROR;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AuthService authService;
    private final LoginService loginService;
    private final UserRepository userRepository;
    private final UserFindService userFindService;

    @PostMapping("/login")
    @Operation(summary = "구글 로그인", description = "구글 로그인 인가 코드를 받아 사용자 인증을 합니다.")
    public ResponseEntity<Map<String, String>> login(@RequestBody TokenDto dto, HttpSession session) {
        String authorizationCode = dto.getToken(); // 클라이언트에서 전달받은 인가 코드
        String redirectUri = dto.getRedirectUri(); // 프론트가 사용한 리다이렉트 URI
        log.info("받은 인가 코드: {}", authorizationCode);

        // authorization code + redirect uri로 사용자 인증
        GoogleInfoDto authenticate = authService.authenticate(authorizationCode, redirectUri);
        Map<String, String> tokens = loginService.processUserLogin(authenticate);

        // 세션 보관(선택): 프론트가 JWT를 관리한다면 세션 사용은 불필요
        session.setAttribute("accessToken", tokens.get("accessToken"));
        session.setAttribute("refreshToken", tokens.get("refreshToken"));
        log.info("구글 사용자 정보: {}", authenticate);

        return ResponseEntity.ok(tokens);  // 액세스 토큰과 리프레시 토큰 반환
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/persona")
    @Operation(summary = "유저 페르소나 기본 설정")
    public ResponseEntity<PersonaResponse> setPersona(
            @RequestBody PersonaRequest req,
            @AuthenticationPrincipal String email){

        PersonaResponse res=userFindService.setPersona(req, email);

        return ResponseEntity.ok(res);
    }
}