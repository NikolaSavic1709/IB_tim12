package com.ib.controller;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.ib.utils.LogIdGenerator.setLogId;


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
    private UserService userService;

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private IUserActivationService userActivationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid JwtAuthenticationRequest authenticationRequest, @RequestHeader HttpHeaders headers) throws Exception {

        setLogId();
        logger.info("Request received successfully /api/login: "+authenticationRequest);
        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }
        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            setLogId();
            logger.error("Returned status NOT_FOUND: User doesn't exist or isn't enabled");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }

        String mfaType = authenticationRequest.getMfaType();
        if (!mfaType.equals("email") && !mfaType.equals("sms")) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: MFA is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MFA is only possible via email or SMS");
        }

        Authentication authentication;
        try {
            endUserService.setStandardAuth(authenticationRequest.getEmail());
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(), authenticationRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        catch(AuthenticationException e){
            setLogId();
            logger.error("Returned status UNAUTHORIZED: Wrong credentials");
            throw e;
        }


        User user = userService.findByEmail(authenticationRequest.getEmail());
        if (user.getLastPasswordResetDate().isBefore(LocalDateTime.now().minusMinutes(1))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Password expired");
        }

        try {
            if(user.getLastMfaDate()==null || user.getLastMfaDate().isBefore(LocalDateTime.now().minusDays(7))) {
                endUserService.sendMFAToken(authenticationRequest.getEmail(), authenticationRequest.getMfaType());
                user.setLastMfaDate(LocalDateTime.now());
                userService.save(user);
                setLogId();
                logger.info("Successfully request /api/login: Returned status OK, proceed to MFA");
                return new ResponseEntity<>("MFA code sent to your resource", HttpStatus.NOT_FOUND);
            }
            else {
                user = (User) authentication.getPrincipal();
                String jwt = tokenUtils.generateToken(user);
                int expiresIn = tokenUtils.getExpiredIn();
                setLogId();
                logger.info("Successfully request /api/loginMFA: Returned status OK, without MFA");
                return ResponseEntity.ok(new JWTToken(jwt, expiresIn));
            }
        } catch (MailSendingException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Error while sending mail, possible inactive email address");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Error while sending sms, possible inactive phone number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        } catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: "+ authenticationRequest);
            throw e;
        }
        finally {
            MDC.remove("logId");
        }

    }

    @PostMapping("/loginMFA")
    public ResponseEntity<?> loginMFA(@RequestBody @Valid MFAAuthenticationRequest authenticationRequest, @RequestHeader HttpHeaders headers) throws Exception {

        setLogId();
        logger.info("Request received successfully /api/loginMFA: "+authenticationRequest);


        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            setLogId();
            logger.error("Returned status NOT_FOUND: User doesn't exist or isn't enabled");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }

        endUserService.setStandardAuth(authenticationRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try{
            endUserService.checkMFA(authenticationRequest.getEmail(),authenticationRequest.getToken());
            User user = (User) authentication.getPrincipal();
            String jwt = tokenUtils.generateToken(user);
            int expiresIn = tokenUtils.getExpiredIn();
            setLogId();
            logger.info("Successfully request /api/loginMFA: Returned status OK");
            return ResponseEntity.ok(new JWTToken(jwt, expiresIn));

        }catch (InvalidUserException e) {
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UserMFAExpiredException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SpamAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: "+ authenticationRequest);
            throw e;
        }
        finally {
            MDC.remove("logId");
        }


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
        setLogId();
        logger.info("Request received successfully /api/logout");
        SecurityContextHolder.getContext().setAuthentication(null);

        setLogId();
        logger.info("Successfully request /api/logout: Returned status OK");
        MDC.remove("logId");
        return ResponseEntity.status(HttpStatus.OK).body("Successful logout.");
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<?> register( @RequestBody RegistrationRequest registrationRequest, @RequestHeader HttpHeaders headers) {
        setLogId();
        logger.info("Request received successfully /api/register: "+registrationRequest);

        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        String activationType = registrationRequest.getUserActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Account activation is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        EndUser newUser = new EndUser(registrationRequest);
        try {
            endUserService.register(newUser, activationType);
            RegistrationDTO registrationDTO=new RegistrationDTO(newUser);
            setLogId();
            logger.info("Successfully request /api/register: Returned status OK, response: "+registrationDTO);
            return new ResponseEntity<>(registrationDTO, HttpStatus.OK);
        } catch (MailSendingException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Error while sending mail, possible inactive email address");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
        } catch (SMSSendingException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Error while sending sms, possible inactive phone number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
        } catch (EmailAlreadyUsedException | PhoneNumberAlreadyUsedException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: "+ registrationRequest);
            throw e;
        }
        finally {
            MDC.remove("logId");
        }

    }

    @PostMapping(value = "/activate")
            public ResponseEntity<?> activateUser(@RequestBody @Valid AccountActivationDTO accountActivationDTO, @RequestHeader HttpHeaders headers) {
        setLogId();
        logger.info("Request received successfully /api/activate: " + accountActivationDTO);

        if (!headers.containsKey("recaptcha")) {
            throw new BadCredentialsException("Invalid reCaptcha token");
        }

        String activationType = accountActivationDTO.getActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Account activation is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        try {
            userActivationService.activate(accountActivationDTO);
            setLogId();
            logger.info("Successfully request /api/activate: Returned status OK");
            return ResponseEntity.status(HttpStatus.OK).body("Successful account activation!");
        } catch (EntityNotFoundException | InvalidActivationResourceException e) {
            setLogId();
            logger.error("Returned status NOT_FOUND: Invalid activation");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid activation");
        } catch (UserActivationExpiredException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: Activation expired. Register again!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Activation expired. Register again!");
        } catch (SpamAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            setLogId();
            logger.error("UNEXPECTED error: " + accountActivationDTO);
            throw e;
        } finally {
            MDC.remove("logId");

        }
    }

    @PostMapping(value="/forgotPassword")
    public ResponseEntity<?> sendResetCodeToEmail(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO, @RequestHeader HttpHeaders headers) {
            setLogId();
            logger.info("Request received successfully /api/forgotPassword: " + forgotPasswordDTO);
            if (!headers.containsKey("recaptcha")) {
                throw new BadCredentialsException("Invalid reCaptcha token");
            }

            String activationType = forgotPasswordDTO.getActivationType();
            if (!activationType.equals("email") && !activationType.equals("sms")) {
                setLogId();
                logger.error("Returned status BAD_REQUEST: Forgot password functionality is only possible via email or SMS");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Forgot password functionality is only possible via email or SMS");
            }
            try {
                endUserService.forgotPassword(forgotPasswordDTO);
                setLogId();
                logger.info("Successfully request /api/forgotPassword: Returned status NO_CONTENT");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Email/SMS with reset code has been sent!");
            } catch (EntityNotFoundException e) {
                setLogId();
                logger.error("Returned status NOT_FOUND: User does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
            } catch (MailSendingException e) {
                setLogId();
                logger.error("Returned status BAD_REQUEST: Error while sending mail, possible inactive email address");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending mail, possible inactive email address");
            } catch (SMSSendingException e) {
                setLogId();
                logger.error("Returned status BAD_REQUEST: Error while sending sms, possible inactive phone number");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending sms, possible inactive phone number");
            } catch (Exception e) {
                setLogId();
                logger.error("UNEXPECTED error: " + forgotPasswordDTO);
                throw e;
            } finally {
                MDC.remove("logId");
            }

        }

        @PostMapping(value = "/resetPassword", consumes = "application/json")
        public ResponseEntity<?> changePasswordWithResetCode (@Valid @RequestBody ResetPasswordDTO
        resetPasswordDTO, @RequestHeader HttpHeaders headers)
        {
            setLogId();
            logger.info("Request received successfully /api/resetPassword: " + resetPasswordDTO);

            if (!headers.containsKey("recaptcha")) {
                throw new BadCredentialsException("Invalid reCaptcha token");
            }

            String activationType = resetPasswordDTO.getActivationType();
            if (!activationType.equals("email") && !activationType.equals("sms")) {
                setLogId();
                logger.error("Returned status BAD_REQUEST: Forgot password functionality is only possible via email or SMS");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Forgot password functionality is only possible via email or SMS");
            }

            try {
                endUserService.resetPassword(resetPasswordDTO);
                setLogId();
                logger.info("Successfully request /api/resetPassword: Returned status NO_CONTENT");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Password successfully changed!");
            } catch (EntityNotFoundException e) {
                setLogId();
                logger.error("Returned status NOT_FOUND: User does not exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
            }  catch (IncorrectCodeException e) {
                setLogId();
                logger.error("Returned status BAD_REQUEST: Wrong reset code");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wrong reset code!");
            } catch (CodeExpiredException e) {
                setLogId();
                logger.error("Returned status BAD_REQUEST: Code is expired");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code is expired!");
            } catch (SpamAuthException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } catch (Exception e) {
                setLogId();
                logger.error("UNEXPECTED error: " + resetPasswordDTO);
                throw e;
            } finally {
                MDC.remove("logId");
            }
        }

        @PostMapping(value = "/renewPassword", consumes = "application/json")
        public ResponseEntity<?> renewPassword (@Valid @RequestBody RenewPasswordDTO
        renewPasswordDTO, @RequestHeader HttpHeaders headers){
            if (!headers.containsKey("recaptcha")) {
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
        public ResponseEntity<?> updateUser (@PathVariable("id") Integer id, @RequestBody UserUpdateDTO userUpdateDTO, @RequestHeader HttpHeaders headers)
        {
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
        public ResponseEntity<?> getUser (@PathVariable("id") Integer id){

            User user = userService.get(id);
            if (user == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid user");

            return new ResponseEntity<>(new UserUpdateDTO(user), HttpStatus.OK);
        }
    }