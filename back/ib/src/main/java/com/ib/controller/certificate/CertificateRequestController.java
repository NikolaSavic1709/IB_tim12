package com.ib.controller.certificate;

import com.ib.DTO.CertificateForRequestDTO;
import com.ib.DTO.RequestCreationDTO;
import com.ib.exception.CertificateCreationException;
import com.ib.exception.ForbiddenException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/request")
public class CertificateRequestController {

    private final IUserService userService;
    private final ICertificateRequestService requestService;

    @Autowired
    public CertificateRequestController(IUserService userService, ICertificateRequestService requestService) {
        this.userService = userService;
        this.requestService = requestService;
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/all/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllRequests(@PathVariable Integer userId, @RequestHeader("Authorization") String token)
    {
        try {
            List<CertificateRequest> requests = requestService.getRequests(userId, token);

            List<CertificateForRequestDTO> requestsDTO = new ArrayList<>();
            for (CertificateRequest req : requests){
                requestsDTO.add(new CertificateForRequestDTO(req));
            }
            return new ResponseEntity<>(requestsDTO, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/create",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRequest(@Valid @RequestBody RequestCreationDTO requestCreationDTO, @RequestHeader("Authorization") String token)
    {
        try {
            CertificateRequest request = requestService.createRequest(requestCreationDTO, token);
            CertificateForRequestDTO requestDTO = new CertificateForRequestDTO(request);
            return new ResponseEntity<>(requestDTO, HttpStatus.OK);
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CertificateCreationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PutMapping(value = "/accept")
    public ResponseEntity<?> acceptRequest(@RequestBody CertificateRequest certificateRequest,  @RequestHeader("Authorization") String token) {
        try {
            Certificate created = requestService.acceptRequest(certificateRequest, token);
            return new ResponseEntity<>(created, HttpStatus.OK);
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PutMapping(value = "/reject/{id}") // id of certificate request
    public ResponseEntity<?> rejectRequest(@RequestBody String rejectionReason, @PathVariable Integer id, @RequestHeader("Authorization") String token) {
        requestService.rejectRequest(id, rejectionReason, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
