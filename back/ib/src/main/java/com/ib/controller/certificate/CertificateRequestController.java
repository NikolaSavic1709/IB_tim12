package com.ib.controller.certificate;

import com.ib.DTO.RequestCreationDTO;
import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateRequest;
import com.ib.service.certificate.interfaces.ICertificateRequestService;
import com.ib.service.users.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/request")
public class CertificateRequestController {

//    private final JwtUtil jwtUtil;
    private final IUserService userService;
    private final ICertificateRequestService requestService;

    @Autowired
    public CertificateRequestController(IUserService userService, ICertificateRequestService requestService) {
        this.userService = userService;
        this.requestService = requestService;
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllRequests(@PathVariable Integer userId, @RequestHeader("Authorization") String authHeader)
    {


        try {
            List<CertificateRequest> requests = requestService.getRequests(userId, authHeader);

            return new ResponseEntity<>(requests, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "User does not exist!");
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid user");
        }
    }

    @PostMapping(value = "/demand",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRequest(@Valid @RequestBody RequestCreationDTO requestCreationDTO, @RequestHeader("Authorization") String authHeader)
    {
        try {

            return new ResponseEntity<>(new Certificate(), HttpStatus.OK);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong data");
        }
    }

}
