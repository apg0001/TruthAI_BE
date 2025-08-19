//package jpabasic.truthaiserver.service;
//
//import jpabasic.truthaiserver.domain.User;
//import jpabasic.truthaiserver.dto.GoogleInfoDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class LoginService {
//    private final UserFindService userFindService;
//    private final UserRegisterService userRegisterService;
//    private final JwtService jwtService;
//
//    public Map<String, String> processUserLogin(GoogleInfoDto googleInfoDto){
//        User user = getOrCreateUser(googleInfoDto);
//        Map<String, String> stringStringMap = generateAuthTokens(user);
//        log.info("accessToken, RefreshToken : {} ", stringStringMap);
//        return stringStringMap;
//    }
//
//    private User getOrCreateUser(GoogleInfoDto googleInfoDto){
//        return userFindService.findUserByEmail(googleInfoDto.getEmail())
//                .orElseGet(() -> userRegisterService.registerUser(googleInfoDto.toUserRegisterDto()));
//    }
//
//    private Map<String, String> generateAuthTokens(User user){
//        Map<String, String> tokens = new HashMap<>();
//        tokens.put("accessToken", jwtService.generateAccessToken(user));
//        tokens.put("refreshToken", jwtService.generateRefreshToken(user));
//        return tokens;
//    }
//}
package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.GoogleInfoDto;
import jpabasic.truthaiserver.service.JwtService;
import jpabasic.truthaiserver.service.UserFindService;
import jpabasic.truthaiserver.service.UserRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginService {

    private final UserFindService userFindService;
    private final UserRegisterService userRegisterService;
    private final JwtService jwtService;

    public Map<String, String> processUserLogin(GoogleInfoDto googleInfoDto) {
        User user = getOrCreateUser(googleInfoDto);
        Map<String, String> stringStringMap = generateAuthTokens(user);
        log.info("발급된 토큰 : {} ", stringStringMap);
        return stringStringMap;
    }

    private User getOrCreateUser(GoogleInfoDto googleInfoDto) {
        return userFindService.findUserByEmail(googleInfoDto.getEmail())
                .orElseGet(() -> userRegisterService.registerUser(googleInfoDto.toUserRegisterDto()));
    }

    private Map<String, String> generateAuthTokens(User user) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtService.generateAccessToken(user));
        tokens.put("refreshToken", jwtService.generateRefreshToken(user));
        return tokens;
    }
}