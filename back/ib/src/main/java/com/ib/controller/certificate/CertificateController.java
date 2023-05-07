package com.ib.controller.certificate;

import com.ib.DTO.CertificateDTO;
import com.ib.DTO.ObjectListResponseDTO;
import com.ib.model.certificate.CertificateStatus;
import com.ib.service.certificate.interfaces.ICertificateService;
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
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "api/certificate")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

    @Autowired
    private ICertificateService certificateService;

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {

        List<CertificateDTO> certificateDTOs = certificateService.getAll().parallelStream()
//                .filter(certificate -> certificate.getStatus().equals(CertificateStatus.VALID))
                .map(CertificateDTO::new).collect(Collectors.toList());
        ObjectListResponseDTO<CertificateDTO> objectListResponse = new ObjectListResponseDTO<>(certificateDTOs.size(), certificateDTOs);
        return new ResponseEntity<>(objectListResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('END_USER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/validity/{serialNumber}")
    public ResponseEntity<?> validateCertificate(@PathVariable String serialNumber) {
        try {
            boolean isValid = certificateService.getAndCheck(serialNumber);
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
            if(certificateService.checkByCopy(file))
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

        // Definišemo zaglavlja
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Expires", "0");

        // Vraćamo odgovor sa datotekom i zaglavljima
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
//        try {
////            CertificateDTO certificateDTO=new CertificateDTO(certificateService.getBySerialNumber(serialNumber));
//            InputStream inputStream=getClass().getResourceAsStream("src/main/resources/certificates/"+serialNumber+".crt");
////            return new ResponseEntity<>(certificateDTO, HttpStatus.OK);
//            return new ResponseEntity<>(new InputStreamResource(inputStream),HttpStatus.OK);
//        }
//        catch (EntityNotFoundException e)
//        {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
    }
}
