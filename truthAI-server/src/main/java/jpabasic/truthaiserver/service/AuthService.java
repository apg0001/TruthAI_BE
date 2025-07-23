package jpabasic.truthaiserver.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;


import jpabasic.truthaiserver.dto.GoogleInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@Slf4j
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    public GoogleInfoDto authenticate(String token) {
        return extractUserInfoFromToken(token);
    }

    private GoogleInfoDto extractUserInfoFromToken(String token) {
        try {
            log.info("token : {}", token);
            GoogleIdTokenVerifier verifier = createGoogleIdTokenVerifier();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            return convertPayloadToGoogleInfoDto(payload);

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Token validation failed", e);
        }
    }

    private GoogleInfoDto convertPayloadToGoogleInfoDto(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        return new GoogleInfoDto(email, name, pictureUrl);
    }

    private GoogleIdTokenVerifier createGoogleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }
}