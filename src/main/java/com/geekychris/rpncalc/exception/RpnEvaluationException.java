package com.geekychris.rpncalc.exception;

/**
 * Thrown when an RPN expression cannot be evaluated, e.g. stack underflow,
 * division by zero, unknown operator, or a non-numeric token.
 */
public class RpnEvaluationException extends RuntimeException {

    public RpnEvaluationException(String message) {
        super(message);
    }
}
