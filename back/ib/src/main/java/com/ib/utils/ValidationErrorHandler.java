package com.ib.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.ib.utils.LogIdGenerator.setLogId;


@RestControllerAdvice
public class ValidationErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ValidationErrorHandler.class);

//    @ExceptionHandler(IllegalArgumentException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    protected ResponseEntity<String> illegalArgument(IllegalArgumentException exception) {
//        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
//    }
//
//
//    @ExceptionHandler(TypeMismatchException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    protected ResponseEntity<String> typeMismatch(TypeMismatchException exception) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
//        return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    protected ResponseEntity<String> typeMismatch(HttpMessageNotReadableException exception) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
//        return new ResponseEntity<>(exception.getMessage(), headers, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    protected ResponseEntity<String> accessDenied(AccessDeniedException exception) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
//
//        return new ResponseEntity<>("Access denied!", headers, HttpStatus.FORBIDDEN);
//    }
//
//    @ExceptionHandler(ExpiredJwtException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    protected ResponseEntity<String> accessDenied(ExpiredJwtException exception) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
//
//        return new ResponseEntity<>("Access denied!", headers, HttpStatus.FORBIDDEN);
//    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<String> invalidRequest(MethodArgumentNotValidException exception){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        setLogId();
        logger.error("Arguments don't valid.");
        MDC.remove("logId");
        return new ResponseEntity<>("Invalid request", headers, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected ResponseEntity<String> f(Exception exception) {
        System.out.println(exception.getMessage());
        return new ResponseEntity<>("Access denied!", HttpStatus.FORBIDDEN);
    }
}
