package com.ib.config;

import com.ib.DTO.RecaptchaResponse;
import com.ib.service.validation.impl.RecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RecaptchaFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;

    public RecaptchaFilter(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("Recaptcha filter called ...");

        if(request.getMethod().equals("POST") ) {
            String recaptcha = request.getHeader("recaptcha");
            if (recaptcha!=null){
                RecaptchaResponse recaptchaResponse = recaptchaService.validateToken(recaptcha);
                if(!recaptchaResponse.getSuccess()) {    // possible score check
                    System.out.println("Invalid reCAPTCHA token");
                    throw new BadCredentialsException("Invalid reCaptcha token");
                }
            }

        }

        filterChain.doFilter(request,response);
    }
}
