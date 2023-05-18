package com.ib.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.ib.DTO.GoogleTokenDTO;
import com.ib.exception.OAuthException;
import com.ib.exception.OAuthUserUnregistered;
import com.ib.model.dto.JWTToken;
import com.ib.model.users.User;
import com.ib.service.users.impl.UserService;
import com.ib.service.users.interfaces.IUserActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
public class OAuthService {

    @Autowired
    private UserService userService;

    @Value("${google.clientId}")
    String googleClientId;

    public User loadUserFromGoogle(GoogleTokenDTO tokenDto) throws OAuthException, OAuthUserUnregistered {
        final NetHttpTransport transport = new NetHttpTransport();
        final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
        GoogleIdTokenVerifier.Builder verifier =
                new GoogleIdTokenVerifier.Builder(transport, jacksonFactory)
                        .setAudience(Collections.singletonList(googleClientId));

        GoogleIdToken googleIdToken;
        try {
            googleIdToken = GoogleIdToken.parse(verifier.getJsonFactory(), tokenDto.getValue());
        } catch (IOException e) {
            throw new OAuthException("Invalid google token");
        }

        if (googleIdToken != null) {
            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("given_name");
            String surname = (String) payload.get("family_name");

            User user = userService.findByEmail(email);
            if (user != null){
                return user;
            } else {
                // ako moze i bez vec kreiranog kreiraj novi
                throw new OAuthUserUnregistered("User not registered");
            }
        } else {
            throw new OAuthException("Invalid google token");
        }
    }
}
