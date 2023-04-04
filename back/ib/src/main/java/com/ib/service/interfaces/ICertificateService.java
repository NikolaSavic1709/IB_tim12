package com.ib.service.interfaces;

import com.ib.model.certificate.Certificate;

public interface ICertificateService extends IJPAService<Certificate>{
    boolean getAndCheck(String id);
}
