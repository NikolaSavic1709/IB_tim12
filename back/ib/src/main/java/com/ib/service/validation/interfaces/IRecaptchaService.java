package com.ib.service.validation.interfaces;

import com.ib.DTO.RecaptchaResponse;

public interface IRecaptchaService {
    RecaptchaResponse validateToken(String recaptchaToken);
}
