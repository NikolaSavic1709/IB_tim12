package com.ib.exception;

public class CertificateAlreadyRevokedException extends Exception{

    public CertificateAlreadyRevokedException(String message){
        super(message);
    }
}
