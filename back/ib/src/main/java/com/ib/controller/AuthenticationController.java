package com.ib.controller;

import com.ib.DTO.*;
import com.ib.exception.*;
import com.ib.model.dto.JWTToken;
import com.ib.model.dto.request.JwtAuthenticationRequest;
import com.ib.model.dto.request.RegistrationRequest;
import com.ib.model.users.EndUser;
import com.ib.model.users.User;
import com.ib.service.EndUserService;
import com.ib.service.users.interfaces.IUserActivationService;
import com.ib.utils.TokenUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


//Kontroler zaduzen za autentifikaciju korisnika
@RestController
@RequestMapping(value = "/api")
@CrossOrigin
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EndUserService endUserService;

    @Autowired
    private IUserActivationService userActivationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid JwtAuthenticationRequest authenticationRequest) throws Exception {

        logger.info("Request received successfully /api/login: "+authenticationRequest);
        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            logger.error("Returned status NOT_FOUND: User doesn't exist or isn't enabled");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }

        String mfaType = authenticationRequest.getMfaType();
        if (!mfaType.equals("email") && !mfaType.equals("sms")) {
            logger.error("Returned status BAD_REQUEST: MFA is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MFA is only possible via email or SMS");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(), authenticationRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        catch(AuthenticationException e){
            logger.error("Returned status UNAUTHORIZED: Wrong credentials");
            throw e;
        }


        try {
            endUserService.sendMFAToken(authenticationRequest.getEmail(), authenticationRequest.getMfaType());
        } catch (MailSendingException e) {
            logger.error("Returned status BAD_REQUEST: Error while sending mail, possible inactive email address");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            logger.error("Returned status BAD_REQUEST: Error while sending sms, possible inactive phone number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        } catch (Exception e){
            logger.error("UNEXPECTED error: "+ authenticationRequest);
            throw e;
        }
        logger.info("Successfully request /api/login: Returned status OK");
        return new ResponseEntity<>("MFA code sent to your resource", HttpStatus.OK);
    }

    @PostMapping("/loginMFA")
    public ResponseEntity<?> loginMFA(@RequestBody @Valid MFAAuthenticationRequest authenticationRequest) throws Exception {
        logger.info("Request received successfully /api/loginMFA: "+authenticationRequest);
        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            logger.error("Returned status NOT_FOUND: User doesn't exist or isn't enabled");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try{
            endUserService.checkMFA(authenticationRequest.getEmail(),authenticationRequest.getToken());
        }catch (InvalidUserException e) {
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserMFAExpiredException e) {
            logger.error("Returned status BAD_REQUEST: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e){
            logger.error("UNEXPECTED error: "+ authenticationRequest);
            throw e;
        }

        User user = (User) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user);
        int expiresIn = tokenUtils.getExpiredIn();
        logger.info("Successfully request /api/loginMFA: Returned status OK");
        return ResponseEntity.ok(new JWTToken(jwt, expiresIn));
    }

    @GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logoutUser () {
        logger.info("Request received successfully /api/logout");
        SecurityContextHolder.getContext().setAuthentication(null);
        logger.info("Successfully request /api/logout: Returned status OK");
        return ResponseEntity.status(HttpStatus.OK).body("Successful logout.");
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register( @RequestBody RegistrationRequest registrationRequest) {
        logger.info("Request received successfully /api/register: "+registrationRequest);
        String activationType = registrationRequest.getUserActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            logger.error("Returned status BAD_REQUEST: Account activation is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        EndUser newUser = new EndUser(registrationRequest);
        try {
            endUserService.register(newUser, activationType);
        } catch (MailSendingException e) {
            logger.error("Returned status BAD_REQUEST: Error while sending mail, possible inactive email address");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            logger.error("Returned status BAD_REQUEST: Error while sending sms, possible inactive phone number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        } catch (EmailAlreadyUsedException | PhoneNumberAlreadyUsedException e) {
            logger.error("Returned status BAD_REQUEST: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e){
            logger.error("UNEXPECTED error: "+ registrationRequest);
            throw e;
        }
        RegistrationDTO registrationDTO=new RegistrationDTO(newUser);
        logger.info("Successfully request /api/register: Returned status OK, response: "+registrationDTO);
        return new ResponseEntity<>(registrationDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<?> activateUser(@RequestBody @Valid AccountActivationDTO accountActivationDTO) {
        logger.info("Request received successfully /api/activate: "+accountActivationDTO);
        String activationType = accountActivationDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            logger.error("Returned status BAD_REQUEST: Account activation is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        try {
            userActivationService.activate(accountActivationDTO);
            logger.info("Successfully request /api/activate: Returned status OK");
            return ResponseEntity.status(HttpStatus.OK).body("Successful account activation!");
        } catch (EntityNotFoundException | InvalidActivationResourceException e) {
            logger.error("Returned status NOT_FOUND: Invalid activation");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid activation");
        } catch (UserActivationExpiredException e) {
            logger.error("Returned status BAD_REQUEST: Activation expired. Register again!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation expired. Register again!");
        } catch (Exception e){
            logger.error("UNEXPECTED error: "+ accountActivationDTO);
            throw e;
        }
    }

    @PostMapping(value="/forgotPassword")
    public ResponseEntity<?> sendResetCodeToEmail(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO)
    {
        logger.info("Request received successfully /api/forgotPassword: "+forgotPasswordDTO);
        String activationType = forgotPasswordDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            logger.error("Returned status BAD_REQUEST: Forgot password functionality is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Forgot password functionality is only possible via email or SMS");
        }
        try {
            endUserService.forgotPassword(forgotPasswordDTO);
            logger.info("Successfully request /api/forgotPassword: Returned status NO_CONTENT");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Email/SMS with reset code has been sent!");
        } catch (EntityNotFoundException e) {
            logger.error("Returned status NOT_FOUND: User does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
        } catch (MailSendingException e) {
            logger.error("Returned status BAD_REQUEST: Error while sending mail, possible inactive email address");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            logger.error("Returned status BAD_REQUEST: Error while sending sms, possible inactive phone number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        } catch (Exception e){
            logger.error("UNEXPECTED error: "+ forgotPasswordDTO);
            throw e;
        }

    }

    @PostMapping(value="/resetPassword", consumes = "application/json")
    public ResponseEntity<?> changePasswordWithResetCode(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO)
    {
        logger.info("Request received successfully /api/resetPassword: "+resetPasswordDTO);
        String activationType = resetPasswordDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            logger.error("Returned status BAD_REQUEST: Forgot password functionality is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Forgot password functionality is only possible via email or SMS");
        }

        try {
            endUserService.resetPassword(resetPasswordDTO);
            logger.info("Successfully request /api/resetPassword: Returned status NO_CONTENT");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Password successfully changed!");
        } catch (EntityNotFoundException e) {
            logger.error("Returned status NOT_FOUND: User does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        } catch (IncorrectCodeException | CodeExpiredException e) {
            logger.error("Returned status BAD_REQUEST: Code is expired or not correct");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code is expired or not correct!");
        } catch (Exception e){
            logger.error("UNEXPECTED error: "+ resetPasswordDTO);
            throw e;
        }
    }
}