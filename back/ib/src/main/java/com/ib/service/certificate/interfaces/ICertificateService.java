package com.ib.service.certificate.interfaces;

import com.ib.model.certificate.Certificate;
import com.ib.service.base.interfaces.IJPAService;

public interface ICertificateService extends IJPAService<Certificate> {
    boolean getAndCheck(String id);
}
