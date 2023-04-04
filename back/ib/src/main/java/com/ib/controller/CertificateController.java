package com.ib.controller;


import com.ib.DTO.CertificateDTO;
import com.ib.DTO.ObjectListResponseDTO;
import com.ib.model.certificate.Certificate;
import com.ib.service.interfaces.ICertificateService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "api/certificate")
public class CertificateController {

    @Autowired
    private ICertificateService certificateService;
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllEmployees() {

        List<CertificateDTO> certificateDTOs = certificateService.getAll().parallelStream().map(CertificateDTO::new).collect(Collectors.toList());
        ObjectListResponseDTO<CertificateDTO> objectListResponse = new ObjectListResponseDTO<>(certificateDTOs.size(), certificateDTOs);
        return new ResponseEntity<>(objectListResponse,HttpStatus.OK);
    }
    @GetMapping(value = "/validity/{serialNumber}")
    public ResponseEntity<?> getAllEmployees(@PathVariable String serialNumber) {

        List<CertificateDTO> certificateDTOs = certificateService.getAll().parallelStream().map(CertificateDTO::new).collect(Collectors.toList());
        ObjectListResponseDTO<CertificateDTO> objectListResponse = new ObjectListResponseDTO<>(certificateDTOs.size(), certificateDTOs);
        try {
            boolean isValid = certificateService.isValid(serialNumber);
            return new ResponseEntity<>(isValid, HttpStatus.OK);
        }
        catch (EntityNotFoundException e)
        {
            return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.NOT_FOUND);
        }
    }
}
