package com.ib.exception;

public class PasswordAlreadyUsedException extends Exception{

    public PasswordAlreadyUsedException(){super("Cannot use previously used password.");}
}

