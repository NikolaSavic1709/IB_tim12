package com.ib.controller;

import com.ib.DTO.AccountActivationDTO;
import com.ib.DTO.RegistrationDTO;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


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
    private IUserActivationService userActivationService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid JwtAuthenticationRequest authenticationRequest) throws Exception {

            if(!endUserService.checkUserEnabled(authenticationRequest.getEmail())){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid login");
            }
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(), authenticationRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();
            String jwt = tokenUtils.generateToken(user);
            int expiresIn = tokenUtils.getExpiredIn();

            return ResponseEntity.ok(new JWTToken(jwt, expiresIn));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register( @RequestBody RegistrationRequest registrationRequest) {
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
    public ResponseEntity<?> activateUser(@RequestBody @Valid AccountActivationDTO accountActivationDTO) {
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
        }
    }
}