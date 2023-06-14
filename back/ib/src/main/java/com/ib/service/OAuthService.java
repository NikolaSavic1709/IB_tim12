package com.ib.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.ib.DTO.GoogleTokenDTO;
import com.ib.exception.OAuthException;
import com.ib.exception.OAuthUserUnregistered;
import com.ib.model.users.Authority;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.repository.users.IEndUserRepository;
import com.ib.repository.users.IUserRepository;
import com.ib.service.users.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Service
public class OAuthService {

    private final  UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final IEndUserRepository endUserRepository;
    private final IUserRepository userRepository;
    private final AuthorityService authorityService;

    @Value("${google.clientId}")
    String googleClientId;


    @Autowired
    public OAuthService(UserService userService, PasswordEncoder passwordEncoder, IEndUserRepository endUserRepository, IUserRepository userRepository, AuthorityService authorityService){
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.endUserRepository = endUserRepository;
        this.userRepository = userRepository;
        this.authorityService = authorityService;
    }

    public synchronized User loadUserFromGoogle(GoogleTokenDTO tokenDto) throws OAuthException, OAuthUserUnregistered {
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
                user.setOauth(true);
                user = userRepository.save(user);
                return user;
            } else {
                // if you can auth without registered account, create one
                EndUser newUser = new EndUser();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setSurname(surname);
                newUser.setEnabled(true);
                newUser.setPasswordHistory(new ArrayList<>());

                Authority authority = authorityService.findByName("END_USER");
                newUser.setAuthority(authority);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setLastPasswordResetDate(LocalDateTime.now());
                newUser.setOauth(true);

                user = endUserRepository.save(newUser);
                return user;
                // if you can not auth without registered account throw exception
                //throw new OAuthUserUnregistered("User not registered");
            }
        } else {
            throw new OAuthException("Invalid google token");
        }
    }
}
