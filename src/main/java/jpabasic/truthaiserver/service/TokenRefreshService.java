package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.TokenRefreshResponse;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public TokenRefreshResponse refreshTokens(String refreshToken) {
        try {
            // 리프레시 토큰에서 사용자 ID 추출
            Long userId = jwtService.getUserIdByParseToken(refreshToken);
            
            // 사용자 존재 여부 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorMessages.USER_NULL_ERROR));

            // 새로운 액세스 토큰과 리프레시 토큰 생성
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            log.info("토큰 재발급 완료 - userId: {}", userId);
            
            return new TokenRefreshResponse(newAccessToken, newRefreshToken, "Bearer");
            
        } catch (Exception e) {
            log.error("토큰 재발급 실패: {}", e.getMessage());
            throw new BusinessException(ErrorMessages.Invalid_Token);
        }
    }
}
