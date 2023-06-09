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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.ib.utils.LogIdGenerator.setLogId;


@RestController
@RequestMapping(value = "/api/request")
@CrossOrigin(origins = "https://localhost:4200")
public class CertificateRequestController {
    private static final Logger logger = LoggerFactory.getLogger(CertificateRequestController.class);
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
        setLogId();
        logger.info("Request received successfully /api/request/all/"+userId);

        try {
            List<CertificateRequest> requests = requestService.getRequests(userId, token);

            List<CertificateForRequestDTO> requestsDTO = new ArrayList<>();
            for (CertificateRequest req : requests){
                requestsDTO.add(new CertificateForRequestDTO(req));
            }
            setLogId();
            logger.info("Successfully request /api/request/all/"+userId+": Returned status OK, response: "+requestsDTO);
            return new ResponseEntity<>(requestsDTO, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidUserException e) {
            setLogId();
            logger.error("Returned status FORBIDDEN: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: userId="+ userId);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/create",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRequest(@Valid @RequestBody RequestCreationDTO requestCreationDTO, @RequestHeader("Authorization") String token, @RequestHeader HttpHeaders headers)
    {
        setLogId();
        logger.info("Request received successfully /api/request/create: "+requestCreationDTO);

        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }
        try {
            CertificateRequest request = requestService.createRequest(requestCreationDTO, token);
            CertificateForRequestDTO requestDTO = new CertificateForRequestDTO(request);
            setLogId();
            logger.info("Successfully request /api/request/create: Returned status OK, response: "+requestDTO);
            return new ResponseEntity<>(requestDTO, HttpStatus.OK);
        } catch (ForbiddenException e) {
            setLogId();
            logger.error("Returned status FORBIDDEN: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CertificateCreationException e) {
            setLogId();
            logger.error("Returned status INTERNAL_SERVER_ERROR: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: "+ requestCreationDTO);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }


    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PutMapping(value = "/accept/{serialNumber}")
    public ResponseEntity<?> acceptRequest(@PathVariable String serialNumber,  @RequestHeader("Authorization") String token) {
        setLogId();
        logger.info("Request received successfully /api/request/accept: "+serialNumber);

        try {
            Certificate created = requestService.acceptRequest(serialNumber, token);
            setLogId();
            logger.info("Successfully request /api/request/accept: Returned status OK, response: "+created);
            return new ResponseEntity<>(created, HttpStatus.OK);
        } catch (ForbiddenException e) {
            setLogId();
            logger.error("Returned status FORBIDDEN: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: "+ serialNumber);
            throw e;
        }finally {
            MDC.remove("logId");
        }

    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PutMapping(value = "/reject/{serialNumber}") // id of certificate request
    public ResponseEntity<?> rejectRequest(@RequestBody String rejectionReason, @PathVariable String serialNumber, @RequestHeader("Authorization") String token) throws ForbiddenException {
        setLogId();
        logger.info("Request received successfully /api/request/reject/"+serialNumber+": rejectionReason: "+rejectionReason);

        try{
            requestService.rejectRequest(serialNumber, rejectionReason, token);
            setLogId();
            logger.info("Successfully request /api/request/reject/"+serialNumber+": Returned status OK");
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: requestId: "+ serialNumber+", rejectionReason: "+ rejectionReason);
            throw e;
        }finally {
            MDC.remove("logId");

        }
    }

}
