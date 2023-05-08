package com.ib.controller.certificate;

import com.ib.DTO.CertificateDTO;
import com.ib.DTO.ObjectListResponseDTO;
import com.ib.exception.CertificateAlreadyRevokedException;
import com.ib.exception.ForbiddenException;
import com.ib.exception.InvalidUserException;
import com.ib.model.certificate.CertificateStatus;
import com.ib.service.certificate.interfaces.ICertificateService;
import com.ib.service.certificate.interfaces.ICertificateValidationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "api/certificate")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

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

        List<CertificateDTO> certificateDTOs = certificateService.getAll().parallelStream()
                .filter(certificate -> certificate.getStatus().equals(CertificateStatus.VALID))
                .map(CertificateDTO::new).collect(Collectors.toList());
        ObjectListResponseDTO<CertificateDTO> objectListResponse = new ObjectListResponseDTO<>(certificateDTOs.size(), certificateDTOs);
        return new ResponseEntity<>(objectListResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/validity/{serialNumber}")
    public ResponseEntity<?> validateCertificate(@PathVariable String serialNumber) {
        try {
            boolean isValid = certificateValidationService.getAndCheck(serialNumber);
            return new ResponseEntity<>(isValid, HttpStatus.OK);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/validity/file")
    public ResponseEntity<?> validateCertificateByCopy(@RequestParam("file") MultipartFile file) {
        try {
            if(certificateValidationService.checkByCopy(file))
                return new ResponseEntity<>(HttpStatus.OK);
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not valid");
        } catch (CertificateException | IOException | EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not valid");

        }

    }
    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/{serialNumber}")
    public ResponseEntity<?> getById(@PathVariable String serialNumber) {
        try {
            CertificateDTO certificateDTO=new CertificateDTO(certificateService.getBySerialNumber(serialNumber));
            return new ResponseEntity<>(certificateDTO, HttpStatus.OK);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/file/{serialNumber}")
    public ResponseEntity<?> getFileBySerialNumber(@PathVariable String serialNumber) {
        File file = new File("src/main/resources/certificates/"+serialNumber+".crt");
        InputStreamResource resource = null;
        try {
            resource = new InputStreamResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @PostMapping(value = "/revoke/{serialNumber}")
    public ResponseEntity<?> revokeCertificate(@PathVariable String serialNumber, @RequestBody String revocationReason, @RequestHeader("Authorization") String token)
    {
        try {
            certificateValidationService.revokeCertificate(serialNumber,revocationReason, token);
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CertificateAlreadyRevokedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
