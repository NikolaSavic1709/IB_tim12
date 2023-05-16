package com.ib.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ib.model.certificate.Certificate;
import com.ib.model.certificate.CertificateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateDTO {
    @NotNull
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date startDate;

    @NotNull
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date endDate;

    @NotNull
    private CertificateType type;

    @NotNull
    @NotEmpty
    @NotBlank
    private String email;

    @NotNull
    @NotEmpty
    @NotBlank
    private String serialNumber;

    public CertificateDTO(Certificate certificate)
    {
        this.email=certificate.getEmail();
        this.startDate=certificate.getStartDate();
        this.endDate=certificate.getEndDate();
        this.type=certificate.getType();
        this.serialNumber = certificate.getSerialNumber();
    }
}
