package com.geekychris.rpncalc.controller;

import com.geekychris.rpncalc.exception.RpnEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RpnEvaluationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CalculationResponse handleEvaluationException(RpnEvaluationException e) {
        return CalculationResponse.failure(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CalculationResponse handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Invalid request");
        return CalculationResponse.failure(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CalculationResponse handleUnreadableMessage(HttpMessageNotReadableException e) {
        return CalculationResponse.failure("Malformed JSON request body");
    }
}
