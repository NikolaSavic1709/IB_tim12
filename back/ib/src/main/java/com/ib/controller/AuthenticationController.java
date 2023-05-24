package com.ib.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.ib.DTO.*;
import com.ib.exception.*;
import com.ib.model.dto.JWTToken;
import com.ib.model.dto.request.JwtAuthenticationRequest;
import com.ib.model.dto.request.RegistrationRequest;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.service.EndUserService;
import com.ib.service.OAuthService;
import com.ib.service.users.impl.UserService;
import com.ib.service.users.interfaces.IUserActivationService;
import com.ib.utils.TokenUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;


//Kontroler zaduzen za autentifikaciju korisnika
@RestController
@RequestMapping(value = "/api")
@CrossOrigin
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EndUserService endUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private IUserActivationService userActivationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid JwtAuthenticationRequest authenticationRequest, @RequestHeader HttpHeaders headers) throws Exception {

        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }
        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }
        User user = userService.findByEmail(authenticationRequest.getEmail());
        if (user.getLastPasswordResetDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Password expired");
        }

        String mfaType = authenticationRequest.getMfaType();
        if (!mfaType.equals("email") && !mfaType.equals("sms")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MFA is only possible via email or SMS");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            endUserService.sendMFAToken(authenticationRequest.getEmail(), authenticationRequest.getMfaType());
        } catch (MailSendingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        }
        return new ResponseEntity<>("MFA code sent to your resource", HttpStatus.OK);
    }

    @PostMapping("/loginMFA")
    public ResponseEntity<?> loginMFA(@RequestBody @Valid MFAAuthenticationRequest authenticationRequest, @RequestHeader HttpHeaders headers) throws Exception {

        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }

        endUserService.setStandardAuth(authenticationRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try{
            endUserService.checkMFA(authenticationRequest.getEmail(),authenticationRequest.getToken());
        }catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserMFAExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SpamAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

        User user = (User) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user);
        int expiresIn = tokenUtils.getExpiredIn();
        return ResponseEntity.ok(new JWTToken(jwt, expiresIn));
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody @Valid GoogleTokenDTO tokenDto){
        try{
            User user = oauthService.loadUserFromGoogle(tokenDto);

            String jwt = tokenUtils.generateToken(user);
            int expiresIn = tokenUtils.getExpiredIn();
            return ResponseEntity.ok(new JWTToken(jwt, expiresIn));

        }catch (OAuthException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (OAuthUserUnregistered e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }


    @GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logoutUser () {
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.status(HttpStatus.OK).body("Successful logout.");
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register( @RequestBody RegistrationRequest registrationRequest, @RequestHeader HttpHeaders headers) {
        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        String activationType = registrationRequest.getUserActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        EndUser newUser = new EndUser(registrationRequest);
        try {
            endUserService.register(newUser, activationType);
        } catch (MailSendingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        } catch (EmailAlreadyUsedException | PhoneNumberAlreadyUsedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return new ResponseEntity<>(new RegistrationDTO(newUser), HttpStatus.OK);
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<?> activateUser(@RequestBody @Valid AccountActivationDTO accountActivationDTO, @RequestHeader HttpHeaders headers) {
        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        String activationType = accountActivationDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        try {
            userActivationService.activate(accountActivationDTO);
            return ResponseEntity.status(HttpStatus.OK).body("Successful account activation!");
        } catch (EntityNotFoundException | InvalidActivationResourceException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid activation");
        } catch (UserActivationExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation expired. Register again!");
        } catch (SpamAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping(value="/forgotPassword")
    public ResponseEntity<?> sendResetCodeToEmail(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO, @RequestHeader HttpHeaders headers) {
        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        String activationType = forgotPasswordDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Forgot password functionality is only possible via email or SMS");
        }
        try {
            endUserService.forgotPassword(forgotPasswordDTO);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Email/SMS with reset code has been sent!");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        } catch (MailSendingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        }

    }

    @PostMapping(value="/resetPassword", consumes = "application/json")
    public ResponseEntity<?> changePasswordWithResetCode(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO, @RequestHeader HttpHeaders headers)
    {
        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        String activationType = resetPasswordDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Forgot password functionality is only possible via email or SMS");
        }

        try {
            endUserService.resetPassword(resetPasswordDTO);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Password successfully changed!");
        } catch (IncorrectCodeException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "Wrong reset code!");
        } catch (CodeExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code is expired!");
        } catch (SpamAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping(value = "/renewPassword", consumes = "application/json")
    public ResponseEntity<?> renewPassword(@Valid @RequestBody RenewPasswordDTO renewPasswordDTO, @RequestHeader HttpHeaders headers) {
        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        try {
            endUserService.renewPassword(renewPasswordDTO);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Password successfully changed!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(value = "/user/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Integer id, @RequestBody UserUpdateDTO userUpdateDTO, @RequestHeader HttpHeaders headers) {
//        if (!headers.containsKey("recaptcha")){
//            throw new BadCredentialsException("Invalid reCaptcha token");
//        }
        User user;
        try {
             user = userService.update(id, userUpdateDTO);
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        return new ResponseEntity<>(new UserUpdateDTO(user), HttpStatus.OK);
    }

    @GetMapping(value = "/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Integer id) {

        User user = userService.get(id);
        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid user");

        return new ResponseEntity<>(new UserUpdateDTO(user), HttpStatus.OK);
    }
}