package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.domain.UserBaseInfo;
import jpabasic.truthaiserver.dto.user.GoogleInfoDto;
import jpabasic.truthaiserver.dto.persona.PersonaRequest;
import jpabasic.truthaiserver.dto.persona.PersonaResponse;
import jpabasic.truthaiserver.dto.user.TokenDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.UserRepository;
import jpabasic.truthaiserver.security.CustomUserDetails;
import jpabasic.truthaiserver.service.LoginService;
import jpabasic.truthaiserver.service.UserFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jpabasic.truthaiserver.service.AuthService;


import java.util.Map;

import static jpabasic.truthaiserver.exception.ErrorMessages.USER_NULL_ERROR;

@RestController
//@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AuthService authService;
    private final LoginService loginService;
    private final UserRepository userRepository;
    private final UserFindService userFindService;

    @PostMapping("/auth/login")
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

    @PostMapping("/auth/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/persona")
    @Operation(summary = "유저 페르소나 기본 설정")
    public ResponseEntity<PersonaResponse> setPersona(
            @RequestBody PersonaRequest req,
            @AuthenticationPrincipal(expression = "user") User user){

        PersonaResponse res=userFindService.setPersona(req, user);

        return ResponseEntity.ok(res);
    }


    @GetMapping("/persona")
    @Operation(summary = "유저 기본 설정한 페르소나 조회")
    public ResponseEntity<PersonaResponse> getPersona(
            @AuthenticationPrincipal(expression="user") User user){

        System.out.println("🥺user"+user.getId());

        UserBaseInfo info=user.getUserBaseInfo();
        String persona=info.getPersona();

        PersonaResponse res=new PersonaResponse(persona);
        return ResponseEntity.ok(res);

    }
}