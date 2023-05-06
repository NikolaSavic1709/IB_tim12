package com.ib.exception;

public class PhoneNumberAlreadyUsedException extends Exception{
    public PhoneNumberAlreadyUsedException(String message){
        super(message);
    }
}
