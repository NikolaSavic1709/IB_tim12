package com.ib.exception;

public class PasswordNotMatchingException extends Exception{

    public PasswordNotMatchingException(){
        super("Passwords don't match");
    }

}
