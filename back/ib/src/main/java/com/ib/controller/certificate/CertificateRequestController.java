package com.ib.controller.certificate;

import com.ib.DTO.CertificateForRequestDTO;
import com.ib.DTO.RequestCreationDTO;
import com.ib.controller.AuthenticationController;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.ib.controller.AuthenticationController.getLastLogId;

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
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/request/all/"+userId);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        try {
            List<CertificateRequest> requests = requestService.getRequests(userId, token);

            List<CertificateForRequestDTO> requestsDTO = new ArrayList<>();
            for (CertificateRequest req : requests){
                requestsDTO.add(new CertificateForRequestDTO(req));
            }
            logger.info("Successfully request /api/request/all/"+userId+": Returned status OK, response: "+requestsDTO);
            return new ResponseEntity<>(requestsDTO, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidUserException e) {
            logger.error("Returned status FORBIDDEN: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }catch (Exception e){
            logger.error("UNEXPECTED error: userId="+ userId);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/create",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRequest(@Valid @RequestBody RequestCreationDTO requestCreationDTO, @RequestHeader("Authorization") String token)
    {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/request/create: "+requestCreationDTO);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        try {
            CertificateRequest request = requestService.createRequest(requestCreationDTO, token);
            CertificateForRequestDTO requestDTO = new CertificateForRequestDTO(request);
            logger.info("Successfully request /api/request/create: Returned status OK, response: "+requestDTO);
            return new ResponseEntity<>(requestDTO, HttpStatus.OK);
        } catch (ForbiddenException e) {
            logger.error("Returned status FORBIDDEN: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CertificateCreationException e) {
            logger.error("Returned status INTERNAL_SERVER_ERROR: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }catch (Exception e){
            logger.error("UNEXPECTED error: "+ requestCreationDTO);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }


    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PutMapping(value = "/accept")
    public ResponseEntity<?> acceptRequest(@RequestBody CertificateRequest certificateRequest,  @RequestHeader("Authorization") String token) {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/request/accept: "+certificateRequest);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        try {
            Certificate created = requestService.acceptRequest(certificateRequest, token);
            logger.info("Successfully request /api/request/accept: Returned status OK, response: "+created);
            return new ResponseEntity<>(created, HttpStatus.OK);
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }catch (Exception e){
            logger.error("UNEXPECTED error: "+ certificateRequest);
            throw e;
        }finally {
            MDC.remove("logId");
        }

    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PutMapping(value = "/reject/{id}") // id of certificate request
    public ResponseEntity<?> rejectRequest(@RequestBody String rejectionReason, @PathVariable Integer id, @RequestHeader("Authorization") String token) {
        String logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        logger.info("Request received successfully /api/request/reject/"+id+": rejectionReason: "+rejectionReason);

        logId = String.valueOf(getLastLogId()+1);
        MDC.put("logId", logId);
        try{
            requestService.rejectRequest(id, rejectionReason, token);
            logger.info("Successfully request /api/request/reject/"+id+": Returned status OK");
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e){
            logger.error("UNEXPECTED error: requestId: "+ id+", rejectionReason: "+ rejectionReason);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }

}
