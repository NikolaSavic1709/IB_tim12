package com.ib.controller.certificate;

import com.ib.DTO.CertificateDTO;
import com.ib.DTO.ObjectListResponseDTO;
import com.ib.exception.CertificateAlreadyRevokedException;
import com.ib.exception.ForbiddenException;
import com.ib.model.certificate.CertificateStatus;
import com.ib.service.certificate.interfaces.ICertificateService;
import com.ib.service.certificate.interfaces.ICertificateValidationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ib.utils.LogIdGenerator.setLogId;


@RestController
@RequestMapping(value = "api/certificate")
@CrossOrigin(origins = "https://localhost:4200")
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);
    private final ICertificateService certificateService;
    private final ICertificateValidationService certificateValidationService;

    @Autowired
    public CertificateController(ICertificateService certificateService, ICertificateValidationService certificateValidationService) {
        this.certificateService = certificateService;
        this.certificateValidationService = certificateValidationService;
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {
        setLogId();
        logger.info("Request received successfully /api/certificate");

        try {
            List<CertificateDTO> certificateDTOs = certificateService.getAll().parallelStream()
                    .filter(certificate -> certificate.getStatus().equals(CertificateStatus.VALID))
                    .map(CertificateDTO::new).collect(Collectors.toList());
            ObjectListResponseDTO<CertificateDTO> objectListResponse = new ObjectListResponseDTO<>(certificateDTOs.size(), certificateDTOs);
            setLogId();
            logger.info("Successfully request /api/certificate: Returned status OK, response: "+objectListResponse);
            return new ResponseEntity<>(objectListResponse, HttpStatus.OK);
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: getAll");
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/validity/{serialNumber}")
    public ResponseEntity<?> validateCertificate(@PathVariable String serialNumber) {
        setLogId();
        logger.info("Request received successfully /api/certificate/validity/"+serialNumber);

        try {
            boolean isValid = certificateValidationService.getAndCheck(serialNumber);
            setLogId();
            logger.info("Successfully request /api/certificate/validity/"+serialNumber+" : Returned status OK, response: "+isValid);
            return new ResponseEntity<>(isValid, HttpStatus.OK);
        }
        catch (EntityNotFoundException e)
        {
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: serialNumber: "+serialNumber);
            throw e;
        } finally {
            MDC.remove("logId");
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/validity/file")
    public ResponseEntity<?> validateCertificateByCopy(@RequestParam("file") MultipartFile file) {
        setLogId();
        logger.info("Request received successfully /api/certificate/validity/file: fileBytes: "+file);

        try {
            if (certificateValidationService.checkByCopy(file)) {
                setLogId();
                logger.info("Successfully request /api/certificate/validity/file: Returned status OK, response: true");
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else {
                setLogId();
                logger.info("Successfully request /api/certificate/validity/file: Returned status OK, response: false");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not valid");
            }
        } catch (CertificateException | IOException | EntityNotFoundException e){
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not valid");
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: fileBytes: "+file);
            throw e;
        } finally {
            MDC.remove("logId");
        }

    }
    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/{serialNumber}")
    public ResponseEntity<?> getById(@PathVariable String serialNumber) {
        setLogId();
        logger.info("Request received successfully /api/certificate/"+serialNumber);

        try {
            CertificateDTO certificateDTO=new CertificateDTO(certificateService.getBySerialNumber(serialNumber));
            setLogId();
            logger.info("Successfully request /api/certificate/"+serialNumber+": Returned status OK, response: "+certificateDTO);
            return new ResponseEntity<>(certificateDTO, HttpStatus.OK);
        }
        catch (EntityNotFoundException e)
        {
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: serialNumber:"+ serialNumber);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/file/{serialNumber}")
    public ResponseEntity<?> getFilesBySerialNumber(@PathVariable String serialNumber, @RequestHeader("Authorization") String token) {
        setLogId();
        logger.info("Request received successfully /api/certificate/file/"+serialNumber);

        try {
            byte[] zipBytes = certificateService.getCertificatesInZip(serialNumber, token);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(zipBytes));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", serialNumber));
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Expires", "0");
            setLogId();
            logger.info("Successfully request /api/certificate/file"+serialNumber+": Returned status OK, response file: "+new BCryptPasswordEncoder().encode(resource.toString()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(zipBytes.length)
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(resource);
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: serialNumber:"+ serialNumber);
            throw e;
        }finally {
            MDC.remove("logId");
        }
    }


    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/revoke/{serialNumber}")
    public ResponseEntity<?> revokeCertificate(@PathVariable String serialNumber, @RequestBody String revocationReason, @RequestHeader("Authorization") String token, @RequestHeader HttpHeaders headers)
    {
        setLogId();
        logger.info("Request received successfully /api/certificate/revoke/"+serialNumber+": revocationReason: "+revocationReason);

        if (!headers.containsKey("recaptcha")){
            throw new BadCredentialsException("Invalid reCaptcha token");
        }
        try {
            certificateValidationService.revokeCertificate(serialNumber,revocationReason, token);
            setLogId();
            logger.info("Successfully request /api/certificate/revoke/"+serialNumber+": Returned status OK");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ForbiddenException e) {
            setLogId();
            logger.error("Returned status FORBIDDEN: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e){
            setLogId();
            logger.error("Returned status NOT_FOUND: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CertificateAlreadyRevokedException e) {
            setLogId();
            logger.error("Returned status BAD_REQUEST: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            setLogId();
            logger.error("UNEXPECTED error: serialNumber:"+ serialNumber+", revocationReason: "+revocationReason);
            throw e;
        }finally {
            MDC.remove("logId");
        }

    }
}
