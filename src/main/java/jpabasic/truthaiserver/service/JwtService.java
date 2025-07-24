package jpabasic.truthaiserver.service;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Service
public class
JwtService {

    //yml 에 정의된 secret,토큰만료 시간
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refreshExpiration}")
    private Long refreshExpiration;

    // 엑세스토큰 발급
    public String generateAccessToken(jpabasic.truthaiserver.domain.User user) {
        return generateToken(user.getId(), expiration);
    }

    //리프레시 토큰 발급
    public String generateRefreshToken(jpabasic.truthaiserver.domain.User user) {
        return generateToken(user.getId(), refreshExpiration);
    }

    //토큰 생성
    private String generateToken(Long userId, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    //토큰 검증
    public boolean validateAccessToken(String accessToken) {
        try {
            return isTokenTimeValid(accessToken);
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            throw new BusinessException(ErrorMessages.Invalid_Token);
        }
    }

    //리프레시 토큰으로 엑세스 토큰 재발급
    public String renewAccessTokenUsingRefreshToken(String refreshToken)  {
        try {
            Claims claims = parseToken(refreshToken).getBody();
            Long userId = Long.valueOf(claims.getSubject());
            return generateToken(userId, expiration);
        } catch (JwtException e) {
            throw new BusinessException(ErrorMessages.Invalid_Token);
        }
    }

    // payload 에 저장된 유저아이디 가지고 오기
    public Long getUserIdByParseToken(String token){
        Jws<Claims> claimsJws = parseToken(token);
        String subject = claimsJws.getBody().getSubject();
        return Long.parseLong(subject);
    }

    private Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);
    }

    // 시간 검증
    private boolean isTokenTimeValid(String token) throws JwtException {
        Jws<Claims> claims = parseToken(token);
        return !claims.getBody().getExpiration().before(new Date());
    }

    // JWT 토큰을 Authorization 헤더에서 추출
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }

    public UserDetails getUserDetailsFromToken(String token) {
        Long userId = getUserIdByParseToken(token);

        return new org.springframework.security.core.userdetails.User(
                userId.toString(), // username
                "", // password는 필요 없으므로 빈 문자열
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}