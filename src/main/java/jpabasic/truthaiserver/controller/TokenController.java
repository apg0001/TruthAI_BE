package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.dto.TokenRefreshRequest;
import jpabasic.truthaiserver.dto.TokenRefreshResponse;
import jpabasic.truthaiserver.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "토큰 관리", description = "JWT 토큰 재발급 관련 API")
public class TokenController {

    private final TokenRefreshService tokenRefreshService;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        log.info("토큰 재발급 요청");
        TokenRefreshResponse response = tokenRefreshService.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
