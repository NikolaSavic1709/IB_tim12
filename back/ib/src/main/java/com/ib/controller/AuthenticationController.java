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
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


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
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/login: "+authenticationRequest);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);


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
            logger.info("Successfully request /api/login: Returned status OK");
            return new ResponseEntity<>("MFA code sent to your resource", HttpStatus.OK);
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
        finally {
            MDC.remove("logId");
        }

    }

    @PostMapping("/loginMFA")
    public ResponseEntity<?> loginMFA(@RequestBody @Valid MFAAuthenticationRequest authenticationRequest) throws Exception {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/loginMFA: "+authenticationRequest);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);

        if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
            logger.error("Returned status NOT_FOUND: User doesn't exist or isn't enabled");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try{
            endUserService.checkMFA(authenticationRequest.getEmail(),authenticationRequest.getToken());
            User user = (User) authentication.getPrincipal();
            String jwt = tokenUtils.generateToken(user);
            int expiresIn = tokenUtils.getExpiredIn();
            logger.info("Successfully request /api/loginMFA: Returned status OK");
            return ResponseEntity.ok(new JWTToken(jwt, expiresIn));
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
        finally {
            MDC.remove("logId");
        }


    }

    @GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logoutUser () {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/logout");
        SecurityContextHolder.getContext().setAuthentication(null);
        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Successfully request /api/logout: Returned status OK");
        MDC.remove("logId");
        return ResponseEntity.status(HttpStatus.OK).body("Successful logout.");
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register( @RequestBody RegistrationRequest registrationRequest) {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/register: "+registrationRequest);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);

        String activationType = registrationRequest.getUserActivationType();
        if (!activationType.equals("email") && !activationType.equals("sms")) {
            logger.error("Returned status BAD_REQUEST: Account activation is only possible via email or SMS");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account activation is only possible via email or SMS");
        }
        EndUser newUser = new EndUser(registrationRequest);
        try {
            endUserService.register(newUser, activationType);
            RegistrationDTO registrationDTO=new RegistrationDTO(newUser);
            logger.info("Successfully request /api/register: Returned status OK, response: "+registrationDTO);
            return new ResponseEntity<>(registrationDTO, HttpStatus.OK);
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
        finally {
            MDC.remove("logId");
        }

    }

    @PostMapping(value = "/activate")
    public ResponseEntity<?> activateUser(@RequestBody @Valid AccountActivationDTO accountActivationDTO) {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/activate: "+accountActivationDTO);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);

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
        finally {
            MDC.remove("logId");
        }
    }

    @PostMapping(value="/forgotPassword")
    public ResponseEntity<?> sendResetCodeToEmail(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO)
    {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/forgotPassword: "+forgotPasswordDTO);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);

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
        finally {
            MDC.remove("logId");
        }

    }

    @PostMapping(value="/resetPassword", consumes = "application/json")
    public ResponseEntity<?> changePasswordWithResetCode(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO)
    {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/resetPassword: "+resetPasswordDTO);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);

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
        finally {
            MDC.remove("logId");
        }
    }


    public static int getLastLogId() {
        String logFilePath = "src/main/resources/logs/application.log";
        String lastId = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lastId = extractLogId(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(lastId!=null)
            return Integer.parseInt(lastId);
        return 0;
    }

    private static String extractLogId(String logEntry) {
        int idStartIndex = logEntry.indexOf("[") + 1;
        int idEndIndex = logEntry.indexOf("]");
        return logEntry.substring(idStartIndex, idEndIndex);
    }
}